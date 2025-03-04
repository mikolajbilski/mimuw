#include "common/io.h"
#include "common/sumset.h"
#include "taskstack.h"

Solution solve(Sumset* a, Sumset* b, InputData* input_data)
{
    Solution solution;
    solution_init(&solution);

    TaskStack stack;
    init_stack(&stack, a, b);

    int d = input_data->d;

    while (!is_empty(&stack)) {
        const Task* current_task = top(&stack);
        if (current_task->for_index <= d) {
            const Sumset* curr_a = current_task->a;
            const Sumset* curr_b = current_task->b;

            bool should_pop = true;

            if (curr_a->last < current_task->for_index || is_sumset_intersection_trivial(curr_a, curr_b)) {
                for (int i = current_task->for_index; i <= d; i++) {
                    if (!does_sumset_contain(curr_b, i)) {
                        should_pop = false;
                        push_new_task(&stack, curr_a, curr_b, i);
                        break;
                    }
                }
            } else if ((curr_a->sum == curr_b->sum) && (get_sumset_intersection_size(curr_a, curr_b) == 2)) {
                if (curr_b->sum > solution.sum) {
                    solution_build(&solution, input_data, curr_a, curr_b);
                }
            }

            if (should_pop) {
                pop(&stack);
            }

        } else {
            pop(&stack);
        }
    }

    destroy(&stack);

    return solution;
}

int main()
{
    InputData input_data;
    input_data_read(&input_data);

    Solution best_solution;
    solution_init(&best_solution);

    best_solution = solve(&input_data.a_start, &input_data.b_start, &input_data);

    solution_print(&best_solution);
    return 0;
}
