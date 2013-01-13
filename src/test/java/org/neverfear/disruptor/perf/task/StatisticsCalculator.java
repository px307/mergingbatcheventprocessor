package org.neverfear.disruptor.perf.task;


public final class StatisticsCalculator {
	private StatisticsCalculator() {
	}

	/**
	 * This object represents statistics generated in this run
	 */
	public static final class Results {
		public final int count;
		public final double mean;
		public final long max;
		public final long min;
		public final double sd;

		public Results(final int count, final double mean, final long max, final long min, final double sd) {
			this.count = count;
			this.mean = mean;
			this.max = max;
			this.min = min;
			this.sd = sd;
		}

		@Override
		public String toString() {
			return "Stats [count=" + this.count + ", mean=" + this.mean + ", max=" + this.max + ", min=" + this.min
					+ ", sd=" + this.sd + "]";
		}

	}

	public static Results calcStats(final long[] dataSet, final int count) {
		long sum = 0;
		long min = Long.MAX_VALUE;
		long max = 0;
		for (int i = 0; i < count; i++) {
			final long dataPoint = dataSet[i];
			sum += dataPoint;
			min = Math.min(dataPoint, min);
			max = Math.max(dataPoint, max);
		}
		final double mean = (double) sum / (double) count;

		double sumDiffFromMean = 0;
		for (int i = 0; i < count; i++) {
			final long dataPoint = dataSet[i];
			sumDiffFromMean += Math.pow(dataPoint - mean, 2);
		}
		final double sd = Math.sqrt(sumDiffFromMean / count);

		return new Results(count, mean, max, min, sd);
	}
}
