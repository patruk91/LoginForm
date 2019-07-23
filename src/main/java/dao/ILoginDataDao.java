package dao;

public interface ILoginDataDao {
    boolean checkIfLoginIsCorrect();

    boolean checkIfPasswordIsCorrect();
}
