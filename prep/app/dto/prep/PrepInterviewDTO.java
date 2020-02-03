package dto.prep;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.prep.PrepInterview;
import models.prep.PrepQuestion;

import com.google.gson.Gson;

public class PrepInterviewDTO extends PrepDTO{

    public Long id;

    public String name;
    
    public boolean valid;

    public List<PrepQuestionDTO> questions;

    public Date created;

    public Date updated;

    public int currentQuestion;

    /**
     * flag that can be set indicating that the interview should be reset so that it can be retaken by the user
     */
    public boolean reset;

    public static PrepInterviewDTO fromJson(String json) {
        try {
            Gson gson = new Gson();
            PrepInterviewDTO pc = gson.fromJson(json, PrepInterviewDTO.class);
            return pc;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static PrepInterviewDTO fromPrepInterview(PrepInterview interview){
        if(interview == null){
            return null;
        }
        PrepInterviewDTO d = new PrepInterviewDTO();
        d.id = interview.id;
        d.name = interview.name;
        d.valid = interview.valid;
        d.created = interview.created;
        d.updated = interview.updated;
        d.currentQuestion = interview.currentQuestion;
        if(interview.questions != null){
            d.questions = new ArrayList<PrepQuestionDTO>();
            for(PrepQuestion q : interview.questions){
                PrepQuestionDTO qd = PrepQuestionDTO.fromPrepQuestion(q);
                if(qd != null){
                    d.questions.add(qd);
                }
            }
        }
        return d;
    }
}