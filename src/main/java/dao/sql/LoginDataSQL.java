package dao.sql;

import dao.ILoginDataDao;

public class LoginDataSQL implements ILoginDataDao {
    private ConnectionPool connectionPool;

    public LoginDataSQL(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }


    @Override
    public boolean checkIfLoginIsCorrect() {
        return false;
    }

    @Override
    public boolean checkIfPasswordIsCorrect() {
        return false;
    }
}
