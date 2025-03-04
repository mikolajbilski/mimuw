add_executable(parallel main.c concurrentstack.c task.c)
target_link_libraries(parallel io err atomic)
