public class Counter {
    private int counterValue = 0;

    public void increment() {
        setCounterValue(counterValue + 1);
    }

    public void increment(int value) {

        int temp = counterValue + value;
        setCounterValue(temp);
    }

    public void setCounterValue(int counterValue) {
        this.counterValue = counterValue;
    }

    public void decrement() {
        setCounterValue(counterValue - 1);
    }
}
