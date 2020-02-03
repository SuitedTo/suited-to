package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Modified version of this:
 * http://yogeshd.blog.com/2010/10/15/iterator-with-item-processing-in-batches/
 *
 * @param <InputType>
 * @param <OutputType>
 */
public abstract class BatchProcessingIterator<InputType, OutputType> implements Iterator<OutputType> {

	private Iterator<List<InputType>> allBatches;
	private Iterator<OutputType> currentBatch;

	public BatchProcessingIterator(List<InputType> batches, int batchSize) {

		this.allBatches = partition(batches, batchSize).iterator();
		this.currentBatch = nextBatch();

	}

	private List<List<InputType>> partition(List<InputType> items, int batchSize) {

		if(batchSize<=0) {

			throw new IllegalArgumentException("Batch Size ["+batchSize+"] should be greater than 0");

		}
		List<List<InputType>> batches = new ArrayList<List<InputType>>();
		List<InputType> batch = new ArrayList<InputType>();
		for(InputType item: items) {

			batch.add(item);
			if(batch.size()==batchSize) {

				batches.add(batch);
				batch = new ArrayList<InputType>();

			}

		}
		if(!batch.isEmpty()){
			batches.add(batch);
		}
		return batches;

	}

	private Iterator<OutputType> nextBatch() {

		return allBatches.hasNext() ? processNextBatch(allBatches.next()) : Collections.<OutputType>emptyList().iterator();

	}

	public abstract Iterator<OutputType> processNextBatch(List<InputType> nextBatch);

	@Override
	public boolean hasNext() {

		while(!currentBatch.hasNext() && allBatches.hasNext()) {

			currentBatch = nextBatch();

		}
		return currentBatch.hasNext();

	}

	@Override
	public OutputType next() {

		return currentBatch.next();

	}


	@Override
	public void remove() {
	}

}
