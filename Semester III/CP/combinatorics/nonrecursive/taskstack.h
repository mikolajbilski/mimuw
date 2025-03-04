#pragma once

#include "common/err.h"
#include "common/sumset.h"

#include <stdlib.h>

const size_t STACK_SIZE = 5000;

typedef struct {
    int for_index;
    const Sumset* a;
    const Sumset* b;
} Task;

typedef struct {
    size_t size;

    Sumset* prevs;
    Task* elements;
} TaskStack;

static inline void init_task(Task* t, const Sumset* a, const Sumset* b, int for_index)
{
    if (a->sum <= b->sum) {
        t->a = a;
        t->b = b;
        t->for_index = for_index;
    } else {
        t->a = b;
        t->b = a;
        t->for_index = b->last;
    }
}

static inline void init_stack(TaskStack* s, const Sumset* a, const Sumset* b)
{
    s->size = 1;
    s->prevs = (Sumset*)malloc(STACK_SIZE * sizeof(Sumset));
    s->elements = (Task*)malloc(STACK_SIZE * sizeof(Task));

    s->prevs[0] = *a;
    init_task(s->elements, s->prevs, b, a->last);
}

static inline void destroy(TaskStack* s)
{
    free(s->elements);
    free(s->prevs);
}

static inline void push_new_task(TaskStack* s, const Sumset* a, const Sumset* b, int i) {
    s->elements[s->size - 1].for_index = i + 1;
    s->prevs[s->size] = *a;
    sumset_add(&s->prevs[s->size], a, i);
    init_task(&s->elements[s->size], &s->prevs[s->size], b, i);
    ++(s->size);
}

static inline void pop(TaskStack* s)
{
    --(s->size);
}

static inline Task* top(TaskStack* s)
{
    return &s->elements[s->size - 1];
}

static inline bool is_empty(TaskStack* s)
{
    return s->size == 0;
}