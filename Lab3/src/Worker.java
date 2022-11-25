class Worker implements Runnable{

    public final MainClass.CommonObject commonObj;
    Worker(MainClass.CommonObject obj) {
        this.commonObj = obj;
    }

    boolean ready;

    public Integer indexStart;
    public Integer indexFinish;
    public int step;
    void setReady(){this.ready = true; this.step = indexStart;};
    void unsetReady(){this.ready = false;};

    void setIndexStart(int index){this.indexStart = index; this.step = index;}
    void setIndexFinish(int index){indexFinish = index;}
    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            if (!ready)
                continue;
            synchronized (this.commonObj) {
                while (step < indexFinish && this.commonObj.commonBuffer.size() > step) {
                    if (this.commonObj.commonBuffer.get(step).toLowerCase()
                            .contains(this.commonObj.substr.toLowerCase())) {
                        int start = this.commonObj.counter + this.step - this.commonObj.commonBuffer.size();
                        for (int i = start  - this.commonObj.beforeFounded; i <= start + this.commonObj.afterFounded; i++)
                        {
                            this.commonObj.numStrings.add(i);
                        }
                    }
                    this.step++;
            }
        }
    }
}
}
