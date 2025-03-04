#include "common/err.h"
#include "concurrentstack.h"

#include <stdlib.h>

void create_stack(ConcurrentStack* s, int thread_count)
{
    s->thread_count = thread_count;
    s->wait_cnt = s->element_count = 0;
    s->finished = false;

    s->capacity = 150 * thread_count;

    ASSERT_ZERO(pthread_cond_init(&s->new_element, NULL));
    ASSERT_ZERO(pthread_mutex_init(&s->lock, NULL));

    s->elements = (Task*)malloc(s->capacity * sizeof(Task));

    if (!s->elements) {
        fatal("malloc");
    }
}

void push(ConcurrentStack* s, Task t)
{
    ASSERT_ZERO(pthread_mutex_lock(&s->lock));

    if (s->element_count == s->capacity) {
        s->capacity *= 2;
        s->elements = (Task*)realloc(s->elements, s->capacity * sizeof(Task));
        if (!s->elements) {
            fatal("realloc");
        }
    }
    s->elements[s->element_count++] = t;

    ASSERT_ZERO(pthread_mutex_unlock(&s->lock));
    ASSERT_ZERO(pthread_cond_signal(&s->new_element));
}

Task pop(ConcurrentStack* s, bool* finished)
{
    ASSERT_ZERO(pthread_mutex_lock(&s->lock));

    while (s->element_count == 0 && (!s->finished)) {
        ++(s->wait_cnt);
        if (s->wait_cnt == s->thread_count) {
            s->finished = true;
            ASSERT_ZERO(pthread_cond_broadcast(&s->new_element));
            break;
        }
        ASSERT_ZERO(pthread_cond_wait(&s->new_element, &s->lock));
        --s->wait_cnt;
    }
    if (s->finished) {
        *finished = true;

        ASSERT_ZERO(pthread_mutex_unlock(&s->lock));
        
        // return anything; it won't be used
        Task t;
        return t;
    }
    Task t = s->elements[--(s->element_count)];
    *finished = false;

    ASSERT_ZERO(pthread_mutex_unlock(&s->lock));
    
    return t;
}

void destroy_stack(ConcurrentStack* s)
{
    ASSERT_ZERO(pthread_mutex_destroy(&s->lock));
    ASSERT_ZERO(pthread_cond_destroy(&s->new_element));
    free(s->elements);
}