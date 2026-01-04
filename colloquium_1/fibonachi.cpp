#include <iostream>
#include <vector>
#include "Utils.cpp"

int main()
{
    try {

        //1
        int n; 
        std::cout << "FIBONACCI\nEnter a natural number (>= 1): "; 
        std::cin >> n; 
        auto fibonacci = Fibonacci(n); 
        std::cout << "The first n Fibonacci numbers are: ";
        for (auto num : fibonacci) { 
            std::cout << num << " ";
        }
        std::cout << std::endl;

        //2
        std::cout << "PALINDROM\nEnter a number: ";
        std::cin >> n; 

        std::cout << std::boolalpha;

        std::cout << isPalindrome(n) << std::endl;

        //3
        std::cout << "LIST\nEnter a list. Enter 'stop' to complete: ";

        std::vector<int> numbers;
        while (std::cin >> n) {
            numbers.push_back(n);
        }

        Node* head = createList(numbers);

        std::cout << "Source list: ";
        printList(head);

        head = reverseList(head);

        std::cout << "The list is as follows: ";
        printList(head);

        deleteList(head);

    }
    catch (const std::exception& e) {
        std::cerr << "Ошибка: " << e.what() << std::endl;
    }

    return 0;
}

