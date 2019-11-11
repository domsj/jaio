/*
 * Jaio: Java API for libaio
 *
 * Author:
 * Jonas Pfefferle <jpf@zurich.ibm.com>
 *
 * Copyright (C) 2016, IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ibm.jaio;

import sun.misc.Unsafe;
import sun.misc.VM;
import sun.nio.ch.DirectBuffer;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class MemoryUtils {
	public MemoryUtils() {
	}

	public static long getAddress(ByteBuffer buffer) {
		return ((DirectBuffer)buffer).address();
	}

	private static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
		Field f = Unsafe.class.getDeclaredField("theUnsafe");
		f.setAccessible(true);
		return (Unsafe) f.get(null);
	}

	private static final Unsafe unsafe;
	static
	{
		try
		{
			unsafe = getUnsafe();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static int pageSize = unsafe.pageSize();

	public static ByteBuffer allocateDirectAlignedBuffer(int pages) {
		int size = pages * pageSize;
		if (VM.isDirectMemoryPageAligned()) {
			return ByteBuffer.allocateDirect(size);
		}

		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size + pageSize - 1);
		long address = getAddress(byteBuffer);
		int offset = (int)(address % pageSize);
		if (offset == 0) {
			return byteBuffer;
		}

		byteBuffer.position(pageSize - offset);
		byteBuffer.limit(pageSize - offset + size);
		return byteBuffer.slice();
	}
}
