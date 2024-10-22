#include <memory>
#include <vector>
#include <unordered_map>
#include "wys.h"

using namespace std;

int n, k, g;

vector<int8_t> reduce_state(const vector<int8_t>& s) {
    vector<int8_t> ans;
        for (auto el : s)
            if (el <= k)
                ans.push_back(el);
    return ans;
}

int convert_question(const vector<int8_t>& s, int q) {
    int x = 0;
    for (int i = 0; (size_t)i < s.size(); ++i)
        if (s[i] <= k)
            if (++x >= q) return i + 1;
    return 0;
}

bool is_final_state(const vector<int8_t>& s) {
    bool flag = false;
    for (auto el : s)
        if (el <= k) {
            if (flag) return false;
            else flag = true;
        }
    return flag;
}

int get_secret(const vector<int8_t>& s) {
    bool flag = false;
    int secret = 0;
    for (int i = 0; (size_t)i < s.size(); ++i) {
        if (s[i] <= k) {
            if (flag) return 0;
            flag = true;
            secret = i;
        }
    }
    return secret;
}

vector<int8_t> updated_state(const vector<int8_t>& s, size_t question, bool ans) {
    vector<int8_t> updated;
    for (auto el : s)
        updated.push_back(el);
    if (ans) {
        for (size_t i = question; i < s.size(); ++i) {
            if (s[i] <= k) {
                ++updated[i];
            }
        }
    }
    else {
        for (size_t i = 0; i < question; ++i) {
            if (s[i] <= k) {
                ++updated[i];
            }
        }
    }
    return updated;
}

long long hash_vector(const vector<int8_t>& v) {
    long long h = v.size();
    h <<= 4;
    for (auto a : v) {
        h <<= 3;
        h += a;
    }
    return h;
}

class Node {
    public:
    shared_ptr<Node> no;
    shared_ptr<Node> yes;
    int8_t q; // which "reasonable question" from left should you ask
    int8_t rating; // min number of questions from this state to get answer
};

shared_ptr<Node> game_root = make_shared<Node>();
unordered_map<long long, shared_ptr<Node>> dp;

shared_ptr<Node> process_state(const vector<int8_t>& s, int8_t depth) {
    long long h = hash_vector(s);
    if (dp.count(h) > 0)
        return dp[h];

    shared_ptr<Node> ans = make_shared<Node>();
    vector<int8_t> reduced = reduce_state(s);
    if (reduced.size() == 1) {
        ans->rating = ans->q = 0;
        ans->no = ans->yes = nullptr;
        dp[h] = ans;
        return ans;
    }
    shared_ptr<Node> no_node;
    shared_ptr<Node> yes_node;
    
    ans->rating = 127;
    int8_t current_rating;
    for (int8_t i = 1; (size_t)i < reduced.size(); ++i) {
        no_node = process_state(updated_state(reduced, i, false), depth + 1);
        if (no_node->rating + 1 < ans->rating) {
            yes_node = process_state(updated_state(reduced, i, true), depth + 1);
            current_rating = max(no_node->rating, yes_node->rating) + 1;
            if (current_rating < ans->rating) {
                ans->no = no_node;
                ans->yes = yes_node;
                ans->q = i;
                ans->rating = current_rating;
            }
        }
    }
    dp[h] = ans;
    return ans;
}

void generate_game_tree() {
    vector<int8_t> beginning = vector<int8_t>(n, 0);
    game_root = process_state(beginning, 0);
    dp.clear();
}

// play a single game
void play() {
    vector<int8_t> curr_state = vector<int8_t>(n, 0);
    shared_ptr<Node> game_state = game_root;
    while (!is_final_state(curr_state)) {
        int question = convert_question(curr_state, game_state->q);
        bool ans = mniejszaNiz(question + 1);
        game_state = ans ? game_state->yes : game_state->no;
        curr_state = updated_state(curr_state, question, ans);
    }
    odpowiedz(get_secret(curr_state) + 1);
}

int main() {
    dajParametry(n, k, g);
    generate_game_tree();
    while (g--) {
        play();
    }
    game_root = nullptr;
}