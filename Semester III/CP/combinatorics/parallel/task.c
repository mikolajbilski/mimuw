#include "task.h"

#include <stdlib.h>

int prev_size(const Sumset* s)
{
    int ans = 1;
    const Sumset* tmp = s;
    while (tmp->prev) {
        tmp = tmp->prev;
        ++ans;
    }

    return ans;
}

void create_task(Task* t, Sumset* a, Sumset* b)
{
    int a_size = prev_size(a);
    int b_size = prev_size(b);

    t->prev_array = malloc((a_size + b_size) * sizeof(Sumset));

    const Sumset* curr = a;
    for (int i = 0; i < a_size; ++i) {
        t->prev_array[i] = *curr;
        if (i > 0) {
            t->prev_array[i - 1].prev = &t->prev_array[i];
        }
        curr = curr->prev;
    }

    curr = b;
    for (int i = a_size; i < a_size + b_size; ++i) {
        t->prev_array[i] = *curr;
        if (i > a_size) {
            t->prev_array[i - 1].prev = &t->prev_array[i];
        }
        curr = curr->prev;
    }

    if (t->prev_array->sum <= t->prev_array[a_size].sum) {
        t->a = t->prev_array;
        t->b = &t->prev_array[a_size];
    } else {
        t->a = &t->prev_array[a_size];
        t->b = t->prev_array;
    }
}

void destroy_task(Task* t)
{
    free(t->prev_array);
}