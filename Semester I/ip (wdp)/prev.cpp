#include <cstdlib>
#include <memory>
#include <climits>
#include "prev.h"

using namespace std;

namespace {

typedef pair<int, int> range;

range half_with_val(range p, int val) { // Returns the half of range p that contains val
    long long s = (long long)p.first + (long long)p.second;
    if (s < 0) --s; // Negative number truncation fix
    long long mid = s / 2;
    if (val <= mid) {
        return make_pair(p.first, mid);
    } else {
        return make_pair(mid + 1, p.second);
    }
}

class Node {
    public:
    
    shared_ptr<Node> left;
    shared_ptr<Node> right;
    int index;
    range val_range;
    
    Node(int indexx, range rg) {
        index = indexx;
        val_range = rg;
        left = right = nullptr;
    }

    int prev(range checked_range) { // Recurrency for calculating prevInRange
        int res = -1;
        if (is_contained(checked_range)) return index;
        if (left != nullptr && left->intersects(checked_range)) res = max(res, left->prev(checked_range));
        if (right != nullptr && right->intersects(checked_range)) res = max(res, right->prev(checked_range));

        return res;
    }

    bool is_leaf() {
        return val_range.first == val_range.second;
    }

    // Creates a vector of all values linked to the branch going from root (this node) to leaf
    vector<shared_ptr<Node>> links(int val) {
        vector<shared_ptr<Node>> s;
        shared_ptr<Node> curr = make_shared<Node>(*this);
        while (curr && !curr->is_leaf()) {
            shared_ptr<Node> next = curr->choose_child_with_val(val);
            if (next == curr->left) {
                s.push_back(curr->right);
            } else {
                s.push_back(curr->left);
            }
            curr = next;
        }
        return s;
    } 

    private:
    
    bool contains_val(int val) {
        return (val >= val_range.first && val <= val_range.second);
    }

    bool is_contained(range r) {
        return (val_range.first >= r.first && val_range.second <= r.second);
    }

    bool intersects(range r) {
        return min(r.second, val_range.second) >= max(r.first, val_range.first);
    }

    shared_ptr<Node> choose_child_with_val(int val) {
        if (left) {
            if (left->contains_val(val)) return left;
            return right;
        } else {
            if (right->contains_val(val)) return right;
            return left;
        }
    }    
};

vector<shared_ptr<Node>> trees;

} // namespace

void init(const vector<int> &seq) {
    for (size_t i = 0; i < seq.size(); ++i) {
        pushBack(seq[i]);
    }
}

int prevInRange(int i, int lo, int hi) {
    return trees[i]->prev(make_pair(lo, hi));
}

void pushBack(int value) {
    size_t indexx = trees.size();
    
    range curr_range = make_pair(INT_MIN, INT_MAX);
    shared_ptr<Node> new_tree = make_shared<Node>(indexx, curr_range);
    shared_ptr<Node> curr = new_tree;
    
    // Create a new branch
    while (!curr->is_leaf()) {
        curr_range = half_with_val(curr_range, value);
        shared_ptr<Node> next = make_shared<Node>(indexx, curr_range);
        if (curr_range.first == curr->val_range.first) {
            curr->left = next;
        } else {
            curr->right = next;
        }
        curr = next;
    }

    // Link old branches (given by links(value)) to the new branch
    if (indexx != 0) {
        vector<shared_ptr<Node>> old_links = trees[indexx - 1]->links(value);
        curr = new_tree;
        int i = 0;
        while (!curr->is_leaf()) {
            shared_ptr<Node> next_link = i < (int)old_links.size() ? old_links[i] : nullptr;
            if (curr->left) {
                curr->right = next_link;
                curr = curr->left;
            } else {
                curr->left = next_link;
                curr = curr->right;
            }
            ++i;
        }
    }
    trees.push_back(new_tree);
}

void done() {
    trees.clear();
}