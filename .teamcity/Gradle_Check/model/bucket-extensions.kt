/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package Gradle_Check.model

import java.util.LinkedList

/**
 * Split a list of elements into nearly even sublist. If an element is too large, largeElementSplitFunction will be used to split the large element into several smaller pieces;
 * if some elements are too small, they will be aggregated by smallElementAggregateFunction.
 *
 * @param list the list to split, must be ordered by size desc
 * @param toIntFunction the function used to map the element to its "size"
 * @param largeElementSplitFunction the function used to further split the large element into smaller pieces
 * @param smallElementAggregateFunction the function used to aggregate tiny elements into a large bucket
 * @param expectedBucketNumber the return value's size should be expectedBucketNumber
 */
fun <T, R> split(list: LinkedList<T>, toIntFunction: (T) -> Int, largeElementSplitFunction: (T, Int) -> List<R>, smallElementAggregateFunction: (List<T>) -> R, expectedBucketNumber: Int, maxNumberInBucket: Int, noElementSplitFunction: (Int) -> List<R> = { throw IllegalArgumentException("More buckets than things to split") }): List<R> {
    if (expectedBucketNumber == 1) {
        return listOf(smallElementAggregateFunction(list))
    }
    if (list.isEmpty()) {
        return noElementSplitFunction(expectedBucketNumber)
    }

    val expectedBucketSize = list.sumBy(toIntFunction) / expectedBucketNumber

    val largestElement = list.removeFirst()!!

    val largestElementSize = toIntFunction(largestElement)

    return if (largestElementSize >= expectedBucketSize) {
        val bucketsOfFirstElement = largeElementSplitFunction(largestElement, if (largestElementSize % expectedBucketSize == 0) largestElementSize / expectedBucketSize else largestElementSize / expectedBucketSize + 1)
        val bucketsOfRestElements = split(list, toIntFunction, largeElementSplitFunction, smallElementAggregateFunction, expectedBucketNumber - bucketsOfFirstElement.size, maxNumberInBucket, noElementSplitFunction)
        bucketsOfFirstElement + bucketsOfRestElements
    } else {
        val buckets = arrayListOf(largestElement)
        var restCapacity = expectedBucketSize - toIntFunction(largestElement)
        while (restCapacity > 0 && list.isNotEmpty() && buckets.size < maxNumberInBucket) {
            val smallestElement = list.removeLast()
            buckets.add(smallestElement)
            restCapacity -= toIntFunction(smallestElement)
        }
        listOf(smallElementAggregateFunction(buckets)) + split(list, toIntFunction, largeElementSplitFunction, smallElementAggregateFunction, expectedBucketNumber - 1, maxNumberInBucket, noElementSplitFunction)
    }
}
