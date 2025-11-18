package ooad1;

public interface Interest {
    double interestRate = 0.0;

    void payInterest();
    void setInterest(double rate);
    String calculateInterest();
    double getInterest();
}