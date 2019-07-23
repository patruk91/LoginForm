package dao;

public interface ISessionDao {
    void insertSessionData(String session, int userId);
    void deleteSessionData(String session);
}
