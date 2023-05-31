package de.ethos.ethoslib.database.hibernate;

import de.ethos.ethoslib.database.Connector;
import de.ethos.ethoslib.database.MySQL;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HibernateConnector<T> extends Connector {
    private final SessionFactory sf;

    public HibernateConnector(@NotNull JavaPlugin plugin, @NotNull MySQL database, @NotNull Configuration config) {
        super(plugin, database);
        this.sf = config.buildSessionFactory();
    }

    public HibernateConnector(@NotNull JavaPlugin plugin, @NotNull MySQL database) {
        this(plugin, database, new Configuration().configure());
    }

    public <T extends Entity> void save(T entity) {
        Transaction tx = null;
        try (Session session = sf.openSession()) {
            tx = session.beginTransaction();
            session.merge(entity.getUniqueId().toString(), entity);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        }
    }

    public <T> T get(Class<T> clazz, UUID uuid) {
        Transaction tx = null;
        try (Session session = sf.openSession()) {
            tx = session.beginTransaction();
            T t = session.get(clazz, uuid.toString());
            tx.commit();
            return t;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        }
    }

}
