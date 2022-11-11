class Worker implements Runnable{

    public final MainClass.CommonObject commonObj;
    Worker(MainClass.CommonObject obj) {
        this.commonObj = obj;
    }
    boolean need;
    boolean ready;

    public Integer indexStart;
    public Integer indexFinish;
    public int step;
    void setNeed(){need = true;};
    void unsetNeed(){need = false;};

    void setReady(){this.ready = true; this.step = indexStart;};
    void unsetReady(){this.ready = false;};

    void setIndexStart(int index){this.indexStart = index; this.step = index; System.out.println("IN THREAD " + Thread.currentThread().getName() +
            "START" + this.indexStart);}
    void setIndexFinish(int index){indexFinish = index; System.out.println("IN THREAD " + Thread.currentThread().getName() +
            "FINISH" + this.indexFinish);}
    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            if (!ready)
                continue;
            synchronized (this.commonObj) {
                System.out.println("Connected");
                while (step < indexFinish && this.commonObj.commonBuffer.size() > step) {
                    if (this.commonObj.commonBuffer.get(step).toLowerCase()
                            .contains(this.commonObj.substr.toLowerCase())) {
                        int start = this.commonObj.counter + this.step - this.commonObj.commonBuffer.size();
                        for (int i = start  - this.commonObj.beforeFounded; i <= start + this.commonObj.afterFounded; i++)
                        {
                            this.commonObj.numStrings.add(i);
                        }
//                    System.out.println("IN THREAD " + Thread.currentThread().getName() + " "
//                            + this.commonObj.commonBuffer.get(this.step)
//                            + "step " + this.commonObj.counter);
                    }
                    this.step++;
            }
        }
    }
}
}
