package app.chaos.job;

import app.Cancellable;
import app.ServentInfo;

public interface Job extends Cancellable {

    ServentInfo getMyServentInfo();
    String getJobID();
}
