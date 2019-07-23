package dao.sql;

import dao.IUserDao;

public class UserSQL implements IUserDao {
    private ConnectionPool connectionPool;

    public UserSQL(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }
}
