import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class Main {
    public static int[] dataArray= new int[30];//0-9 PB, 10-19 MB, 20-29 FB
    public static char[] branchArray= new char[30];
    public static void main(String[] args) {

    }
}

class Buffer {
    private int[] storage;
    private int[] filled;
    private String[] branchData;
    private int size;
    private static Lock lock= new ReentrantLock();
    private static Condition infoStorage = lock.newCondition();

    public Buffer (){
        this.filled= new int[]{0, 10, 20};// pbfill, mbfill, fbfill
        storage= new int[30];
        branchData = new String[30];
        for (int i = 0; i<30; i++) {
            storage[i] = 0;
            branchData[i]=null;
        }
    }

    public void print(){
        for (int i =0; i<30;i++)
            System.out.println("x["+ i +"}="+storage[i]);
        System.out.println("END OF BUFFER.PRINT");
    }

    public void storeData(int amt, int value, int type, String branch){//type=0 PB, 1=MB, 2=FB
        this.lock.lock();
        try{
            if (type==0) {
                while ((this.filled[0] + amt - 1) > 9) {//awaits for space to insert data
                    this.infoStorage.await();
                }
                for (int i = this.filled[0]; i < this.filled[0] + amt - 1; i++) {//inserts data
                    this.storage[i] = value;
                    this.branchData[i] = branch;
                }
                this.filled[0] += amt;
                print();
            } else if (type==1){
                while ((this.filled[1] + amt - 1) > 19) {//awaits for space to insert data
                    this.infoStorage.await();
                }
                for (int i = this.filled[1]; i < this.filled[1] + amt - 1; i++) {//inserts data
                    this.storage[i] = value;
                    this.branchData[i] = branch;
                }
                this.filled[1] += amt;
                print();
            } else {
                while ((this.filled[2] + amt - 1) > 29) {//awaits for space to insert data
                    this.infoStorage.await();
                }
                for (int i = this.filled[2]; i < this.filled[2] + amt - 1; i++) {//inserts data
                    this.storage[i] = value;
                    this.branchData[i] = branch;
                }
                this.filled[2] += amt;
                print();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {//unlocks to allow the next thread to be let in.
            this.infoStorage.signalAll();
            lock.unlock();
        }
    }

    public void deleteData(int amt, int type){
        lock.lock();
        try{
            if (type==0) {
                if (this.filled[0]-amt-1<0){
                    while (this.filled[0] < 1) infoStorage.await();
                    int clearUpper=this.filled[0]-1;
                    for (int i = clearUpper; i>=0;i--){
                        storage[i]=0;
                        branchData[i]=null;
                    }
                    print();
                }else {

                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

        }
    }
}