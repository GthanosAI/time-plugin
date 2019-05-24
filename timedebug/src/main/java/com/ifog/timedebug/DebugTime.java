package com.ifog.timedebug;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author: created by hewei
 * @date: 2019/5/18
 */

@Target(ElementType.METHOD)
public @interface DebugTime {
}
