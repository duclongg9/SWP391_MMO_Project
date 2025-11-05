package queue;

public interface OrderWorker {

    void handle(OrderMessage msg);
}
