package uk.co.optimisticpanda.configtaskchain;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import rx.Observable;
import rx.schedulers.Schedulers;

public enum TaskDecorators {
	;

	public static <T> Observable<T> task(Callable<T> callable) {
		return Observable.fromCallable(callable);
	}
	
	public static <T, R> DependentTask<T, R> dependentTask(Function<T, R> f) {
		return arg -> Observable.fromCallable(() -> f.apply(arg));
	}
	
	public static <T> UnaryDependentTask<T> unaryDependentTask(UnaryOperator<T> f) {
		return arg -> Observable.fromCallable(() -> f.apply(arg));
	}
	
	public interface DependentTask<T, R> {
		Observable<R> build(T arg);  
	} 
	
	public interface UnaryDependentTask<T> extends DependentTask<T, T> {
	}
	
	public static <T> Observable<T> inParrallel(Observable<T> obs) {
		return obs.subscribeOn(Schedulers.newThread());
	}
	
	public static <T> Observable<T> retry(int times, Observable<T> obs) {
		return obs.retry(times);
	}
	
	public static <T> Observable<T> ignoreAnyFailure(Observable<T> obs) {
		return obs.onExceptionResumeNext(Observable.empty());
	}

	public static <T, U, R> Observable<R> zip(Observable<T> obs, Observable<U> other, BiFunction<T, U, R> f) {
		return obs.zipWith(other, f::apply);
	}
	
	public static <T, R> Observable<R> thenPerform(Observable<T> obs, DependentTask<T, R> f) {
		return obs.flatMap(v -> f.build(v));
	}
	
	public static <T, U> Observable<U> then(Observable<T> obs, Function<T, U> f) {
		return obs.map(v -> f.apply(v));
	}
}
