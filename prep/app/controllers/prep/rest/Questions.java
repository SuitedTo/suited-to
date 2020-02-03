package controllers.prep.rest;


import static play.libs.F.Option.None;
import static play.libs.F.Option.Some;
import controllers.prep.access.PrepRestrictedResource;
import models.prep.PrepAnswerVideo;
import models.prep.PrepQuestion;
import models.prep.ewrap.Filters;
import models.prep.ewrap.Page;
import models.prep.ewrap.QueryResult;
import models.prep.ewrap.QueryResultBuilder;
import models.prep.ewrap.QueryResultBuilderProvider;
import models.prep.ewrap.Search;
import models.prep.ewrap.Sort;
import models.prep.queries.filters.questions.Accessible;
import play.data.binding.As;
import play.data.validation.Required;
import play.libs.F;
import play.libs.F.Option;
import data.binding.types.prep.FilterBinder;
import data.binding.types.prep.JsonBinder;
import data.binding.types.prep.PageBinder;
import data.binding.types.prep.PathBinder;
import data.binding.types.prep.SearchBinder;
import data.binding.types.prep.SortBinder;
import dto.prep.PrepAnswerVideoDTO;
import dto.prep.PrepQuestionDTO;
import enums.prep.VideoStatus;

public class Questions extends PrepController{
	
	@PrepRestrictedResource(resourceClassName = "models.prep.PrepQuestion")
	public static void get(Long id){
		if(id == null){
			notFound();
		}
		 PrepQuestion question = PrepQuestion.find.byId(id);
		 if(question == null){
			 notFound();
		 }
		 
		 renderRefinedJSON(question.toJsonObject());
	}
	
    public static void update(@Required @As(binder = JsonBinder.class) PrepQuestionDTO body) {

        PrepQuestion question = PrepQuestion.find.byId(body.id);
        question.answers = body.answers;
        question.videoUuid = body.videoUuid;
        question.save();
    }
    
    public static void list(
    		@As(binder=PathBinder.class) Option<String> path,
    		@As(binder=FilterBinder.class) Option<Filters> filters,
    		@As(binder=SearchBinder.class) Option<Search> search,
    		@As(binder=SortBinder.class) Option<Sort> sort,
    		@As(binder=PageBinder.class) Option<Page> page) {
    	
    	QueryResultBuilder<PrepQuestion> builder =
    			QueryResultBuilderProvider.getQueryResultBuilder(PrepQuestion.class);
    	
    	QueryResult<PrepQuestion> result = 
    			builder.applyFilter((F.Option) Some(new Accessible<PrepQuestion>()), (F.Option) None())
    	    	.applyFilters(filters)
    	    	.applySearch(search)
    	    	.applySort(sort)
    	    	.setPage(page)
    	    	.getResult();
    	
    	renderRefinedJSON(result.asJson(path));
    }
    

	
	@PrepRestrictedResource(resourceClassName = "models.prep.PrepQuestion")
	public static void getVideo(final Long id){
		if(id == null){
			notFound();
		}
		/*
		 * Support long polling for video. For browser clients, html5 is required
		 * to record video in prep so technically we should be able to employ websockets here to notify
		 * those clients in a more scalable way that the video is stored/available but Heroku isn't supporting
		 * websockets yet. So we long poll...but Heroku cuts each request off after 30 seconds without a response
		 * so we gmust give up before then.
		 * 
		 */
		final int SECONDS_TO_WAIT = 25;
		PrepQuestion question = PrepQuestion.find.byId(id);
		PrepAnswerVideo video = PrepAnswerVideo.find.where().eq("question_id", id).findUnique();
		int i = 0;
		while(((VideoStatus.PENDING == question.videoStatus) || (video == null)) && (i < SECONDS_TO_WAIT)){
			await(1000);
			video = PrepAnswerVideo.find.where().eq("question_id", id).findUnique();
			question.refresh();
			++i;
		}
		question.refresh();
		if(VideoStatus.PENDING == question.videoStatus){
			ok();
		}
		if(video == null){
			notFound();
		}
		
		PrepAnswerVideoDTO dto = PrepAnswerVideoDTO.fromAnswerVideo(video);
		dto = (PrepAnswerVideoDTO) await(dto.loadURL());
		renderRefinedJSON(dto);
	}
	
	@PrepRestrictedResource(resourceClassName = "models.prep.PrepQuestion")
	public static void deleteVideo(final Long id){
		if(id == null){
			notFound();
		}
		PrepQuestion question = PrepQuestion.find.byId(id);
		if(question == null){
			notFound();
		}
		question.videoStatus = VideoStatus.UNAVAILABLE;
		question.save();
		
		PrepAnswerVideo video = PrepAnswerVideo.find.where().eq("question_id", id).findUnique();
		if(video == null){
			notFound();
		}
		video.delete();
		
		PrepAnswerVideoDTO dto = PrepAnswerVideoDTO.fromAnswerVideo(video);
		dto = (PrepAnswerVideoDTO) await(dto.loadURL());
		renderRefinedJSON(dto);
	}
}
