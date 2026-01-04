#include "pch.h"
#include "CppUnitTest.h"
#include <vector>
#include "C:\my projects\ОС\fibonachi\fibonachi\Utils.h"
#include "C:\my projects\ОС\fibonachi\fibonachi\Utils.cpp"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace UtilsTest
{
	TEST_CLASS(UtilsTest)
	{
	public:
		
        TEST_METHOD(TestFibonacci)
        {
            std::vector<unsigned long long> expectedFib = { 0, 1, 1, 2, 3 };
            auto result = Fibonacci(5);

            Assert::AreEqual(expectedFib.size(), result.size());
            for (size_t i = 0; i < expectedFib.size(); ++i) {
                Assert::AreEqual(expectedFib[i], result[i]);
            }
        }

        TEST_METHOD(TestPalindrome)
        {
            Assert::IsTrue(isPalindrome(121));
            Assert::IsTrue(isPalindrome(1221));
            Assert::IsTrue(isPalindrome(0));
            Assert::IsFalse(isPalindrome(123));
            Assert::IsFalse(isPalindrome(-121));
        }

        TEST_METHOD(TestReverseList)
        {
            std::vector<int> input = { 1, 2, 3 };
            Node* head = createList(input);
            Node* reversed = reverseList(head);

            std::vector<int> expectedRev = { 3, 2, 1 };
            Node* current = reversed;

            for (int val : expectedRev) {
                Assert::IsNotNull(current);
                Assert::AreEqual(val, current->value);
                current = current->next;
            }

            Assert::IsNull(current); 
            deleteList(reversed);
        }

        TEST_METHOD(TestEmptyList)
        {
            std::vector<int> input = {};
            Node* head = createList(input);
            Assert::IsNull(head);

            Node* reversed = reverseList(head);
            Assert::IsNull(reversed);
        }

        TEST_METHOD(TestSingleElementList)
        {
            std::vector<int> input = { 42 };
            Node* head = createList(input);
            Node* reversed = reverseList(head);

            Assert::IsNotNull(reversed);
            Assert::AreEqual(42, reversed->value);
            Assert::IsNull(reversed->next);

            deleteList(reversed);
        }
	};
}
