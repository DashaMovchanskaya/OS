
#include <vector>
#include <stdexcept> 
#include <iostream>

std::vector<unsigned long long> Fibonacci(int n) {
    if (n <= 0) {
        throw std::invalid_argument("n must be a natural number (n >= 1)");
    }

    std::vector<unsigned long long> result;
    result.reserve(n);

    if (n >= 1) {
        result.push_back(0);
    }
    if (n >= 2) {
        result.push_back(1);
    }

    for (int i = 2; i < n; ++i) {
        unsigned long long next = result[i - 1] + result[i - 2];
        if (next < result[i - 1]) {
            throw std::overflow_error("Fibonacci numbers overflow");
        }
        result.push_back(next);
    }

    return result;
}

bool isPalindrome(int number) {
    if (number < 0) {
        return false;
    }
    if (number < 10) {
        return true;
    }

    int original = number;
    int reversed = 0;

    while (number > 0) {
        int digit = number % 10;

        if (reversed > (2147483647 - digit) / 10) {
            throw std::overflow_error("Overflow at the reverse of the number");
        }

        reversed = reversed * 10 + digit;
        number /= 10;
    }

    return original == reversed;
}

struct Node {
    int value;
    Node* next;
    Node(int x) : value(x), next(nullptr) {}
};

Node* reverseList(Node* head) {
    Node* prev = nullptr;
    Node* current = head;
    Node* next = nullptr;

    while (current != nullptr) {
        next = current->next;
        current->next = prev;
        prev = current;
        current = next;
    }

    return prev;
}

void printList(Node* head) {
    Node* current = head;
    while (current != nullptr) {
        std::cout << current->value;
        if (current->next != nullptr) {
            std::cout << " ";
        }
        current = current->next;
    }
    std::cout << std::endl;
}

Node* createList(const std::vector<int>& values) {
    if (values.empty()) return nullptr;

    Node* head = new Node(values[0]);
    Node* current = head;

    for (size_t i = 1; i < values.size(); ++i) {
        current->next = new Node(values[i]);
        current = current->next;
    }

    return head;
}

void deleteList(Node* head) {
    while (head != nullptr) {
        Node* temp = head;
        head = head->next;
        delete temp;
    }
}
