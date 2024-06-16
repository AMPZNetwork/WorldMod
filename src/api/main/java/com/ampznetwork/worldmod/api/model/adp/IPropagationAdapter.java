package com.ampznetwork.worldmod.api.model.adp;

public interface IPropagationAdapter {
    IPropagationAdapter DUMMY = new IPropagationAdapter() {
        @Override
        public void cancel() {
        }

        @Override
        public void force() {
        }
    };

    void cancel();

    void force();
}
