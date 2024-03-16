package com.build.demo.task;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

/**
 * 输出文件位置：src/main/resources/result.txt
 */
@Component
public class ProduceUnevenTask {

    public static ThreadFactory nameThread = new CustomizableThreadFactory("build-pool-");
    public static ExecutorService threadPool = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(2000), nameThread,
            new ThreadPoolExecutor.CallerRunsPolicy());
    public static ConcurrentHashMap<Integer, Integer> result = new ConcurrentHashMap<>();
    CompletableFuture<Integer> future = CompletableFuture.completedFuture(null);

    /**
     * 并行提交，串行执行2000个任务
     */
    public void serialTask() {
        for (int i = 0; i < 2000; i++) {
            int position = i + 1;
            future = future.supplyAsync(() -> {
                return result.put(position, produceUnevenNumberSum());
            }, threadPool).exceptionally(throwable -> {
                System.out.println("当前线程：" + Thread.currentThread().getId() + ",执行了发生异常");
                return null;
            });
        }
        //任务执行结束后，打印所有结果
        future.thenRunAsync(() -> {
            output(result, "src/main/resources", "result.txt");
        });
    }

    /**
     * 取消未执行的任务
     *
     * @return
     */
    public boolean cancelTask() {
        boolean flag = future.cancel(true);
        output(result, "src/main/resources", "result.txt");
        return flag;
    }

    /**
     * 生成1万个随机数,并计算奇数的总数
     *
     * @return
     */
    public Integer produceUnevenNumberSum() {
        Integer sum = 0;
        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            int randomNumber = random.nextInt();
            if (randomNumber % 2 != 0) {
                sum++;
            }
        }
        return sum;
    }

    /**
     * 将map结果转换成string格式，换行处理
     *
     * @param map 结果
     * @return 输出string
     */
    public String mapToString(Map<Integer, Integer> map) {
        String result = "";
        if (null == map) {
            return result;
        }
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            result += entry.getKey() + ":" + entry.getValue() + "\n";
        }
        return result;
    }

    /**
     * 输出结果到指定文件
     *
     * @param map           结果
     * @param directoryPath 目录
     * @param fileName      文件
     */
    public void output(Map<Integer, Integer> map, String directoryPath, String fileName) {
        String content = mapToString(map);
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, fileName);
        if (file.exists()) {
            file.delete();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
            System.out.println("内容已成功写入文件：" + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("写入文件时出现错误：" + e.getMessage());
        }
    }
}
