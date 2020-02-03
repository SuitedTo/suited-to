
package enums;

import java.util.HashMap;
import java.util.Map;

public enum ActiveInterviewState {
    NOT_STARTED, STARTED, FINISHED;

    private static final  Map<ActiveInterviewState, ActiveInterviewState> currentStateToFutureState = new HashMap<ActiveInterviewState, ActiveInterviewState>();

    static {
        currentStateToFutureState.put(ActiveInterviewState.NOT_STARTED, ActiveInterviewState.STARTED);
        currentStateToFutureState.put(ActiveInterviewState.STARTED, ActiveInterviewState.FINISHED);
        currentStateToFutureState.put(ActiveInterviewState.FINISHED, null);
    }

    /**
     * Get the next ActiveInterviewState in the typical workflow.  May be null, which indicates that there is no next
     * state
     * @return ActiveInterviewState
     */
    public ActiveInterviewState getDefaultNextState(){
        return currentStateToFutureState.get(this);
    }
}
