package com.bgsoftware.superiorskyblock.utils.threads;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class Executor {

    private static SuperiorSkyblockPlugin plugin;
    private static ExecutorService databaseExecutor;
    private static boolean shutdown = false;

    public static void init(SuperiorSkyblockPlugin plugin){
        Executor.plugin = plugin;
        databaseExecutor = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setNameFormat("SuperiorSkyblock Database Thread %d").build());
    }

    public static void ensureMain(Runnable runnable){
        if(shutdown)
            return;

        if(!Bukkit.isPrimaryThread()){
            sync(runnable);
        }
        else{
            runnable.run();
        }
    }

    public static BukkitTask sync(Runnable runnable){
        if(shutdown)
            return null;

        return sync(runnable, 0);
    }

    public static BukkitTask sync(Runnable runnable, long delay){
        if(shutdown)
            return null;

        return Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    public static void data(Runnable runnable){
        if(shutdown)
            return;

        databaseExecutor.execute(runnable);
    }

    public static boolean isDataThread(){
        return Thread.currentThread().getName().contains("SuperiorSkyblock Database Thread");
    }

    public static void async(Runnable runnable){
        if(shutdown)
            return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public static void async(Runnable runnable, long delay){
        if(shutdown)
            return;

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    public static void close(){
        try{
            shutdown = true;
            System.out.println("Shutting down database executor");
            databaseExecutor.shutdown();
            System.out.println("Waiting for database executor...");
            databaseExecutor.awaitTermination(1, TimeUnit.MINUTES);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
