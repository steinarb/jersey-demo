/*
 * Copyright 2018-2021 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package no.priv.bang.demos.jerseyinkaraf.webapi.resources;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import no.priv.bang.demos.jerseyinkaraf.servicedef.Counter;
import no.priv.bang.demos.jerseyinkaraf.servicedef.beans.Count;

class CounterResourceTest {

    @Test
    void testGetCurrentValue() {
        CounterResource resource = new CounterResource();

        // Mock a Counter OSGi service and simulate injection
        Counter counter = mock(Counter.class);
        when(counter.currentValue()).thenReturn(new Count(103));
        resource.counter = counter;

        Count currentValue = resource.currentValue();
        assertEquals(103, currentValue.getCount());
    }

    @Test
    void testGetIncrementedValue() {
        CounterResource resource = new CounterResource();

        // Mock a Counter OSGi service and simulate injection
        Counter counter = mock(Counter.class);
        when(counter.increment()).thenReturn(new Count(104));
        resource.counter = counter;

        Count incrementedValue = resource.increment();
        assertEquals(104, incrementedValue.getCount());
    }

}
