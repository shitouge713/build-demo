package com.build.demo.task;

import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 输出文件位置：src/main/resources/result.txt
 */
@Component
public class ProduceUnevenTask {

    private static final ExecutorService threadPool = Executors.newSingleThreadExecutor();
    public static ConcurrentHashMap<Integer, Integer> result = new ConcurrentHashMap<>();

    /**
     * 并行提交，串行执行2000个任务
     */
    public void serialTask() {
        CompletableFuture<Integer> future = CompletableFuture.completedFuture(null);
        for (int i = 0; i < 2000; i++) {
            int position = i + 1;
            future.thenRunAsync(() -> {
                //防止任务执行太快，取消不及时
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                result.put(position, produceUnevenNumberSum());
            }, threadPool).exceptionally(throwable -> {
                System.out.println("当前任务：" + position + ",执行过程中被取消");
                return null;
            });
        }
        //任务执行结束后，打印所有结果
        new CompletableFuture<>().thenRunAsync(() -> {
            output(result, "src/main/resources", "result.txt");
        });
    }

    /**
     * 取消未执行的任务
     *
     * @return
     */
    public boolean cancelTask() {
        threadPool.shutdownNow();
        output(result, "src/main/resources", "result.txt");
        return true;
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
