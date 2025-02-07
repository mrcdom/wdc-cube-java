package br.com.wedocode.framework.commons.util;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.wedocode.framework.commons.concurrent.ScheduledExecutorDelegate;
import br.com.wedocode.framework.commons.function.ThrowingFunction;

public class PromiseDemo {

    private static Logger LOG = LoggerFactory.getLogger(PromiseDemo.class);

    private static final ScheduledExecutorDelegate executor = new ScheduledExecutorDelegate();

    public static void main(String[] args) throws Exception {
        LOG.info("PromiseDemo");

        var scheduledExecutor = new br.com.wedocode.framework.commons.concurrent.ScheduledExecutorForTestSyncDirect();
        // var scheduledExecutor = new
        // br.com.wedocode.framework.commons.concurrent.ScheduledExecutorForTestSyncDelayed();
        // var scheduledExecutor = new br.com.wedocode.framework.commons.concurrent.ScheduledExecutorForTestAsync(10);
        try {
            executor.setImpl(scheduledExecutor);

            // demo1(8);
            // demo2();
            // demo3();
            demo4();

            scheduledExecutor.flush();
        } finally {
            scheduledExecutor.shutdown();
        }
    }

    static <T> Promise<T> Promise_resolve(T value) {
        return new Promise<T>((resolve, reject) -> {
            executor.schedule(() -> resolve.acceptThrows(value), Duration.ofSeconds(1));
        });
    }

    static <T> Promise<T> Promise_reject(Throwable caught) {
        return new Promise<T>((resolve, reject) -> {
            executor.schedule(() -> reject.acceptThrows(caught), Duration.ofSeconds(1));
        });
    }

    static <T> Promise<T> Promise_test(T value, Throwable caught, int type, int testType) {
        if (type == testType) {
            return Promise_reject(caught);
        } else {
            return Promise_resolve(value);
        }
    }

    static void console_log(Object value) {
        System.out.println(value);
    }

    static void demo1(int testType) throws Exception {

        new Promise<String>((resolve, reject) -> {
            executor.schedule(() -> {
                if (testType == 0) {
                    reject.accept(new Exception("Error"));
                } else {
                    resolve.accept("Ok");
                }
            }, Duration.ofSeconds(1));
        })
                //
                .then(value -> {
                    var tag = "then-0(" + value + ")";
                    System.out.println(tag);
                    return Promise_test(tag, new Throwable(tag), 1, testType);
                })
                //
                .then(value -> {
                    var tag = "then-1(" + value + ")";
                    System.out.println(tag);
                    return Promise_test(tag, new Throwable(tag), 2, testType);
                })
                //
                .then(value -> {
                    var tag = "then-2(" + value + ")";
                    System.out.println(tag);
                    return Promise_test(tag, new Throwable(tag), 3, testType);
                })
                //
                .then(value -> {
                    var tag = "then-3(" + value + ")";

                    if (testType == 4) {
                        System.out.println(tag + " return exception");
                        throw new Exception("From then-3 testType=" + testType);
                    } else {
                        System.out.println(tag + " return null");
                    }
                    return null;
                })
                //
                .then(value -> {
                    var tag = "then-4(" + value + ")";

                    if (testType == 5) {
                        System.out.println(tag + " throw exception");
                        throw new Exception("From then-4 testType=5");
                    } else if (testType == 6) {
                        System.out.println(tag + " return exception");
                        return Promise_reject(new Exception("From then-4 testType=6"));
                    } else {
                        System.out.println(tag + " return null");
                    }
                    return null;
                })
                //
                .catch_(caught -> {
                    var tag = "catch-0(" + caught.getMessage() + ")";

                    if (testType == 6) {
                        System.err.println(tag + " return exception");
                        return Promise.reject(new Exception("From catch-1 - test 6"));
                    } else if (testType == 7) {
                        System.err.println(tag + " throw exception");
                        throw new Exception("From catch-1 - test 7");
                    }

                    System.err.println(tag + " return null");

                    return null;
                })
                //
                .then(value -> {
                    var tag = "then-5(" + value + ")";
                    System.out.println(tag + " return " + tag);
                    return Promise_resolve("tag");
                })
                //
                .then(value -> {
                    var tag = "then-6(" + value + ")";
                    System.out.println(tag + " return null");
                    return null;
                })
                //
                .catch_(caught -> {
                    System.err.println("catch-1 (" + caught.getMessage() + ") return null");
                    return null;
                })
                //
                .then(value -> {
                    var tag = "then-7(" + value + ")";
                    System.out.println(tag + " return null");
                    return null;
                })
                //
                .finally_(() -> {
                    System.out.println("finally-0");
                    if (testType == 8) {
                        throw new Exception("From finally-0 testType=8");
                    }
                })
                //
                .then(value -> {
                    var tag = "then-8(" + value + ")";
                    System.out.println(tag + " return null");
                    return null;
                })
                //
                .finally_(() -> {
                    System.out.println("finally-1");
                });
    }

    static void demo2() {
        new Promise<String>((resolve, reject) -> {
            executor.schedule(() -> {
                var tag = "00";
                console_log(tag);
                resolve.accept(tag);
            }, Duration.ofSeconds(1));
        })
                //
                .then(value -> {
                    console_log("01");

                    return new Promise<String>((resolve, reject) -> {
                        executor.schedule(() -> {
                            console_log("02");
                            resolve.accept("a");
                        }, Duration.ofSeconds(1));
                    })
                            //
                            .then(valueA -> {
                                console_log("03(" + valueA + ")");
                                return Promise_resolve("b");
                            })
                            //
                            .then(valueA -> {
                                console_log("04(" + valueA + ")");
                                return Promise_resolve("c");
                            });
                })
                //
                .finally_(() -> {
                    console_log("AAAAAA");
                    throw new Exception("xAAAAA");
                })
                //
                .catch_(value -> {
                    console_log("11x(" + value + ")");
                    return Promise_resolve("i");
                })
                //
                .finally_(() -> console_log("BBBBBB"))
                //
                .then(value -> {
                    console_log("05");

                    return new Promise<String>((resolve, reject) -> {
                        executor.schedule(() -> {
                            console_log("06");
                            resolve.accept("d");
                        }, Duration.ofSeconds(1));
                    })
                            //
                            .then(valueA -> {
                                console_log("07(" + valueA + ")");
                                return Promise_resolve("e");
                            })
                            //
                            .then(valueA -> {
                                console_log("08(" + valueA + ")");
                                return Promise_resolve("f");
                            });
                })
                //
                .<String>catch_(value -> {
                    console_log("11(" + value + ")");
                    return Promise_resolve("i");
                })
                //
                .then(value -> {
                    console_log("09(" + value + ")");
                    return Promise_reject(new Exception("g"));
                })
                //
                .then(value -> {
                    console_log("10(" + value + ")");
                    return Promise_resolve("h");
                })
                //
                .catch_(value -> {
                    console_log("12(" + value + ")");
                    return Promise_resolve("j");
                })
                //
                .finally_(() -> {
                    console_log("13()");
                })
                //
                .then(value -> {
                    console_log("14(" + value + ")");
                    return Promise_resolve("l");
                });
    }

    static void demo3() {
        ThrowingFunction<Integer, Promise<Integer>> newResolve = PromiseDemo::<Integer>Promise_resolve;

        //@formatter:off
		newResolve.apply(0)
			.then(value0 -> { 
				console_log("level-0: " + value0);
				return newResolve.apply(value0 + 1)
					.then(value1 -> {
						console_log("level-1: " + value1);
						return newResolve.apply(value1 + 1)
							.then(value2 -> { 
								console_log("level-2: " + value2);
								return newResolve.apply(value2 + 1)
										.then(value3 -> {
											console_log("level-3: " + value3);
											return null;
										});
							});
						});
			});
		//@formatter:on
    }

    static void demo4() {
        //@formatter:off
		var promise0 = Promise_resolve(0)
				.then(value -> {
					console_log("promise0: " + value);
					return null;
				});

		var promise1 = Promise_resolve(1)
				.then(value -> {
					console_log("promise1: " + value);
					return null;
				});
		
		var promise2 = Promise_resolve(2)
				.then(value -> {
					console_log("promise2: " + value);
					return Promise_resolve("Promise 2 = " + value);
				});
		//@formatter:on

        promise0.then(promise1).then(promise2).then(value -> {
            console_log("demo4: " + value);
            return null;
        });
    }
}
