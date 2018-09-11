
package com.goluk.a6.common.util;

/**
 * WorkReq abstract class which can be inserted to WorkThread to run in a seperated thread
 */
public interface WorkReq
{
	public void execute();
	public void cancel();
}
