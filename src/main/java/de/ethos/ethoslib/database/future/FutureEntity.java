package de.ethos.ethoslib.database.future;


import de.ethos.ethoslib.EthosLib;
import de.ethos.ethoslib.database.Connector;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Task to call early to get Object when required later
 * Example: PlayerJoinEvent, ChunkLoadEvent, ServerStartUP
 */

public class FutureEntity<T> {
    private final UUID uuid;
    private final Connector connector;
    private final Builder<T> builder;

    private final Class<T> classT;

    private Future<T> future;

    public FutureEntity(Connector connector, UUID uuid ,Builder<T> builder, Class<T> classT) {
        this.uuid = uuid;
        this.builder = builder;
        this.connector = connector;
        this.classT = classT;
    }
    public FutureEntity(JavaPlugin plugin, UUID uuid , Builder<T> builder, Class<T> classT) {
        this.uuid = uuid;
        this.builder = builder;
        this.connector = EthosLib.getINSTANCE().getConnector(plugin);
        this.classT = classT;
    }

    public void runAsync(){
        this.future =  Executors.newSingleThreadExecutor().submit(this::build);
    }

    public T get(){
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    private T build() {
        try {
            ResultSet set = connector.search(tablePrefix -> "SELECT * FROM " + tablePrefix + classT.getSimpleName().toUpperCase()  +   " WHERE uuid='" + uuid.toString() +"'");
            set.next();
            return builder.build(set);
        } catch (SQLException e){
            EthosLib.error(e);
            return null;
        }
    }



}
