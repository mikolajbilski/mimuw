#include "common/err.h"
#include "common/io.h"
#include "common/sumset.h"
#include "concurrentstack.h"

#include <stdlib.h>

const int THRESHOLD = 25000;

typedef struct {
    ConcurrentStack* s;
    InputData* in_data;
} Args;

static void solve(Sumset* a, Sumset* b, Args* t, Solution* best_solution, int* counter)
{
    if (a->sum > b->sum) {
        solve(b, a, t, best_solution, counter);
        return;
    }

    const int d = t->in_data->d;

    if (is_sumset_intersection_trivial(a, b)) {
        for (int i = a->last; i <= d; ++i) {
            if (!does_sumset_contain(b, i)) {
                ++(*counter);
                Sumset a_with_i;
                sumset_add(&a_with_i, a, i);

                if ((*counter) >= THRESHOLD) {
                    Task new_task;
                    create_task(&new_task, &a_with_i, b);
                    push(t->s, new_task);
                    *counter -= THRESHOLD;
                } else {
                    solve(&a_with_i, b, t, best_solution, counter);
                }
            }
        }
    } else if ((a->sum == b->sum) && (get_sumset_intersection_size(a, b) == 2)) {
        if (b->sum > best_solution->sum) {
            solution_build(best_solution, t->in_data, a, b);
        }
    }
}

void* thread(void* ptr)
{
    Args* args = (Args*)ptr;
    int counter = THRESHOLD * 100;
    bool finished = false;

    Solution* s = malloc(sizeof(Solution));
    solution_init(s);

    Task t = pop(args->s, &finished);
    while (!finished) {
        solve(t.a, t.b, args, s, &counter);
        destroy_task(&t);
        t = pop(args->s, &finished);
    }

    pthread_exit(s);
}

int main()
{
    InputData input_data;
    input_data_read(&input_data);
    // input_data_init(&input_data, 8, 10, (int[]){0}, (int[]){1, 0});

    ConcurrentStack stack;
    create_stack(&stack, input_data.t);

    Args args;
    args.in_data = &input_data;
    args.s = &stack;

    Task t;
    create_task(&t, &input_data.a_start, &input_data.b_start);
    push(&stack, t);

    pthread_t* thread_array = malloc(input_data.t * sizeof(pthread_t));
    for (int i = 0; i < input_data.t; ++i) {
        ASSERT_ZERO(pthread_create(&thread_array[i], NULL, thread, &args));
    }

    Solution* best_solution = malloc(sizeof(Solution));
    solution_init(best_solution);
    Solution* thread_solution;
    for (int i = 0; i < input_data.t; ++i) {
        ASSERT_ZERO(pthread_join(thread_array[i], (void**)&thread_solution));
        if (best_solution->sum < thread_solution->sum) {
            *best_solution = *thread_solution;
        }
        free(thread_solution);
    }

    free(thread_array);
    destroy_stack(&stack);

    solution_print(best_solution);
    free(best_solution);

    return 0;
}