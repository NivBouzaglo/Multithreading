package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.services.GPUService;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */

    enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Model model;
    private final Cluster cluster;
    private Queue<DataBatch> batches;
    private BlockingDeque<DataBatch> processed;
    private int processedData;
    private int capacity = 0;
    private GPUService GPU;
    private int ticks;

    public GPU(String t) {
        this.setType(t);
        cluster = Cluster.getInstance();
        batches = new LinkedList<>();
        processed = new LinkedBlockingDeque<>();
        processedData = 0;
    }

    public String getType() {
        if (type == Type.RTX3090) return "RTX3090";
        else if (type == Type.RTX2080) return "RTX2080";
        else if (type == Type.GTX1080) return "GTX1080";
        return null;
    }

    public void setModel(Model model) {
        this.model = model;
    }
    public Queue<DataBatch> getBatches(){
        return batches;
    }



    public Model getModel() {
        return model;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public BlockingDeque getProcessed() {
        return processed;
    }

    public GPUService getGPU() {
        return GPU;
    }

    public Queue<DataBatch> getDataBatchList() {
        return batches;
    }

    public void setType(String t) {
        if (t.compareTo("RTX3090") == 0) {
            type = Type.RTX3090;
            capacity = 32;
            ticks = 1;
        } else if (t.compareTo("RTX2080") == 0) {
            type = Type.RTX2080;
            capacity = 16;
            ticks = 2;
        } else if (t.compareTo("GTX1080") == 0) {
            type = Type.GTX1080;
            capacity = 8;
            ticks = 4;
        }
    }

    /**
     * @pre batch!=null
     * @inv batches!=null.
     * @post batches.size()--.
     */
    public void sendToCluster() {
        if (!batches.isEmpty())
           cluster.sendToCPU(batches.poll());
    }

    /**
     * @pre model.data!=null
     * @inv
     * @post All the data is stores in one of the data batch.
     */
    public void divide() {
        for (int i = 1; i <= model.getData().getSize() / 1000; i++) {
            DataBatch dataBatch = new DataBatch(model.getData(), i * 1000);
            dataBatch.setGpu(this);
            batches.add(dataBatch);
            if (i < capacity/2)
                sendToCluster();
        }

    }
    public String getName() {
        return GPU.getName();
    }

    /**
     * @pre batches!=null
     * @inv model.status="Training".
     * * @post model.status = "Trained".
     */

    public void subTrain() {
        processedData++;
        processed.poll();
        if (processedData * 1000 >= model.getData().getSize()) {
            processedData = 0;
            model.endTraining();
            cluster.getStatistics().addNames(model.getName());
        }
        else if (!batches.isEmpty()) {
            sendToCluster();
        }
    }

    /**
     * @pre
     * @inv
     * @post batches!=null
     */


    public void addTime() {
        if (!processed.isEmpty()) {
            DataBatch currBatch = processed.peek();
            currBatch.setTickCounter();
            cluster.getStatistics().setUnit_used_gpu();
            if (currBatch.getTickCounter()>=ticks){
                subTrain();
            }
        }
    }

    public void setGPU(GPUService s) {
        this.GPU = s;
    }

    public Model testGPU(Model model) {
        double rand = Math.random();
        if (model.getStudent().getStatus().equals(Student.Degree.PhD)){
            if (rand>=0.8)
                model.setResult(Model.result.Good);
            else
                model.setResult(Model.result.Bad);
        }
        if (model.getStudent().getStatus().equals(Student.Degree.MSc)){
            if (rand>=0.6)
                model.setResult(Model.result.Good);
            else
                model.setResult(Model.result.Bad);
        }
        model.setStatus(Model.status.Tested);
        return model;
    }

}