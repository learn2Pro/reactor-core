/*
 * Copyright (c) 2011-2018 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BiFunction;

import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

/**
 * @author Simon Baslé
 */
/*
 * The following comment is a operator codification meant to be searchable.
 * See https://github.com/reactor/reactor-core/issues/1673 for a
 * complete description of each element codified and the associated values.
 *
 * {REQUEST_SHAPING}: NONE
 * {PREFETCH}: NONE
 * {BUFFERING}: NONE
 * {GEOMETRY}: SIDE-EFFECT
 * {SOURCE}: GROUPEDFLUX
 * {OTHER}: serviceability
 */
final class GroupedLift<K, I, O> extends GroupedFlux<K, O> implements Scannable {

	final BiFunction<Publisher, ? super CoreSubscriber<? super O>, ? extends CoreSubscriber<? super I>>
			lifter;

	final GroupedFlux<K, I> source;

	GroupedLift(GroupedFlux<K, I> p,
			BiFunction<Publisher, ? super CoreSubscriber<? super O>, ? extends CoreSubscriber<? super I>> lifter) {
		this.source = Objects.requireNonNull(p, "source");
		this.lifter = lifter;
	}

	@Override
	public int getPrefetch() {
		return source.getPrefetch();
	}

	@Override
	public K key() {
		return source.key();
	}

	@Override
	@Nullable
	public Object scanUnsafe(Attr key) {
		if (key == Attr.PARENT) {
			return source;
		}
		if (key == Attr.PREFETCH) {
			return getPrefetch();
		}

		return null;
	}

	@Override
	public void subscribe(CoreSubscriber<? super O> actual) {
		CoreSubscriber<? super I> input =
				lifter.apply(source, actual);

		Objects.requireNonNull(input, "Lifted subscriber MUST NOT be null");

		source.subscribe(input);
	}
}
