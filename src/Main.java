import java.io.Reader;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class Main {

    public static void main(String[] args) {
        ExecutorService executorService= Executors.newFixedThreadPool(2);
        Buffer buffer=new Buffer();

        Store store = new Store(buffer);
        Remove remove = new Remove(buffer);

        executorService.execute(store);
        executorService.execute(remove);

        executorService.shutdown();
        buffer.print();
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
        storage= new int[30];//0-9 PB, 10-19 MB, 20-29 FB
        branchData = new String[30];//0-9 PB, 10-19 MB, 20-29 FB
        for (int i = 0; i<30; i++) {
            storage[i] = 0;
            branchData[i]=null;
        }
    }

    public void print(){
        for (int i =0; i<30;i++)
            System.out.println("x["+ i +"}="+storage[i] +",\t x["+i+"]= "+branchData[i]);
        System.out.println("END OF BUFFER.PRINT");
    }

    public void storeData(int amt, int value, int type, String branch){//type=0 PB, 1=MB, 2=FB
        this.lock.lock();
        try{
            if (type==0) {
                while ((this.filled[0] + amt - 1) > 9) {//awaits for space to insert data
                    this.infoStorage.await();
                }
                for (int i = this.filled[0]; i <= this.filled[0] + amt - 1; i++) {//inserts data
                    this.storage[i] = value;
                    this.branchData[i] = branch;
                }
                this.filled[0] += amt;
                print();
            } else if (type==1){
                while ((this.filled[1] + amt - 1) > 19) {//awaits for space to insert data
                    this.infoStorage.await();
                }
                for (int i = this.filled[1]; i <= this.filled[1] + amt - 1; i++) {//inserts data
                    this.storage[i] = value;
                    this.branchData[i] = branch;
                }
                this.filled[1] += amt;
                print();
            } else {
                while ((this.filled[2] + amt - 1) > 29) {//awaits for space to insert data
                    this.infoStorage.await();
                }
                for (int i = this.filled[2]; i <= this.filled[2] + amt - 1; i++) {//inserts data
                    this.storage[i] = value;
                    this.branchData[i] = branch;
                }
                this.filled[2] += amt;
                System.out.println("PRINTING FROM addData function");
                print();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {//unlocks to allow the next thread to be let in.
            this.infoStorage.signalAll();
            lock.unlock();
        }

    }

    public void deleteData(int amt, int type, String deleteThis){
        lock.lock();
        try{
            if (type==0) {
                if (this.filled[0]-amt-1<0){// this is either delete amt or delete all in the area.
                    while (this.filled[0] < 1) infoStorage.await();
                    int clearUpper=this.filled[0]-1;
                    for (int i = clearUpper; i>=0;i--){
                        if (branchData[i].equals(deleteThis)) {
                            storage[i] = 0;
                            branchData[i] = null;
                        }
                    }

                }else {
                    int ifmt= this.filled[0]-amt, infmt2=this.filled[0]-1;
                    for (int i=infmt2; i>= ifmt; i--){
                        if (branchData[i].equals(deleteThis)) {
                            storage[i] = 0;
                            branchData[i] = null;
                        }
                    }
                }
                this.filled[0]-=amt;
            } else if (type==1){
                if (this.filled[1]-amt-1<10){// this is either delete amt or delete all in the area.
                    while (this.filled[1] < 11) infoStorage.await();
                    int clearUpper=this.filled[1]-1;
                    for (int i = clearUpper; i>=0;i--){
                        if (branchData[i].equals(deleteThis)) {
                            storage[i] = 0;
                            branchData[i] = null;
                        }
                    }

                }else {
                    int ifmt= this.filled[1]-amt, infmt2=this.filled[1]-1;
                    for (int i=infmt2; i>= ifmt; i--){
                        if (branchData[i].equals(deleteThis)) {
                            storage[i] = 0;
                            branchData[i] = null;
                        }
                    }
                }
                this.filled[1]-=amt;
            } else {
                if (this.filled[2]-amt-1<20){// this is either delete amt or delete all in the area.
                    while (this.filled[2] < 21) infoStorage.await();
                    int clearUpper=this.filled[2]-1;
                    for (int i = clearUpper; i>=20;i--){
                        if (branchData[i].equals(deleteThis)) {
                            storage[i] = 0;
                            branchData[i] = null;
                        }
                    }

                }else {
                    int ifmt= this.filled[2]-amt, infmt2=this.filled[2]-1;
                    for (int i=infmt2; i>= ifmt; i--){
                        if (branchData[i].equals(deleteThis)) {
                            storage[i] = 0;
                            branchData[i] = null;
                        }
                    }
                }
                this.filled[2]-=amt;
                System.out.println("PRINTING FROM addData function");
                print();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            infoStorage.signalAll();
            lock.unlock();
        }
    }
}

class Store implements Runnable{
    Buffer buffer;
    public Store(Buffer buffer){
        this.buffer=buffer;
    }
    public void run(){
        int[] intAmtStore= {5,6,10,8,6};
        int[] intStore={-3,78,22,13,75};
        String[] branchStore={"PB1","FB2","MB3","PB1","FB4",};
        int[] type={0,2,1,0,2};
        for (int i=0; i<intAmtStore.length; i++){
            this.buffer.storeData(intAmtStore[i],intStore[i],type[i],branchStore[i]);
            Thread.yield();
        }
    }
}

class Remove implements Runnable{
    Buffer buffer=new Buffer();

    public Remove (Buffer buffer){
        this.buffer=buffer;
    }

    public void run(){
        int[] amtDelete={2,2,4,3};
        int[] type= {2,0,1,0};
        String[] branchDelete = {"FB2","PB1","MB3","PB1"};
        for (int i=0; i< amtDelete.length;i++){
            this.buffer.deleteData(amtDelete[i],type[i],branchDelete[i]);
            Thread.yield();
        }
    }

}