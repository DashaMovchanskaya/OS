#include "pch.h"
#include "CppUnitTest.h"
#include "functions.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace Lab2tests
{
	TEST_CLASS(Lab2tests)
	{
	public:
		TEST_METHOD(TestIsStrictInteger)
		{
			Assert::IsTrue(isStrictInteger("42"));
			Assert::IsTrue(isStrictInteger("-17"));
			Assert::IsTrue(isStrictInteger("+8"));
			Assert::IsTrue(isStrictInteger("007"));

			Assert::IsFalse(isStrictInteger("42abc"));
			Assert::IsFalse(isStrictInteger("abc42"));
			Assert::IsFalse(isStrictInteger("1.2"));
			Assert::IsFalse(isStrictInteger("3.14"));
			Assert::IsFalse(isStrictInteger(" "));
			Assert::IsFalse(isStrictInteger(""));
			Assert::IsFalse(isStrictInteger("++5"));
			Assert::IsFalse(isStrictInteger("--3"));
			Assert::IsFalse(isStrictInteger("12 34"));

			Assert::IsTrue(isStrictInteger(std::to_string(INT_MAX)));
			Assert::IsTrue(isStrictInteger(std::to_string(INT_MIN)));
		}

		TEST_METHOD(TestMin_MaxThread)
		{
			size = 5;
			numbers = new int[size] { 1, 2, 3, 4, 5 };

			HANDLE hMin_max = CreateThread(NULL, 0, min_max, NULL, 0, NULL);
			Assert::IsNotNull(hMin_max);
			WaitForSingleObject(hMin_max, INFINITE);

			Assert::AreEqual(1, minValue);
			Assert::AreEqual(5, maxValue);

			CloseHandle(hMin_max);
			delete[] numbers;
			numbers = nullptr;
		}

		TEST_METHOD(TestAverageThread)
		{
			size = 5;
			numbers = new int[size] { 1, 2, 3, 4, 5 };

			HANDLE hAverage = CreateThread(NULL, 0, average, NULL, 0, NULL);
			Assert::IsNotNull(hAverage);
			WaitForSingleObject(hAverage, INFINITE);

			Assert::AreEqual(3.0, averageValue, 0.001);

			CloseHandle(hAverage);
			delete[] numbers;
			numbers = nullptr;
		}

		TEST_METHOD(TestReplaceMinMaxWithAverage)
		{
			size = 5;
			numbers = new int[size] { 1, 2, 3, 4, 5 };

			HANDLE hMin_max = CreateThread(NULL, 0, min_max, NULL, 0, NULL);
			HANDLE hAverage = CreateThread(NULL, 0, average, NULL, 0, NULL);

			Assert::IsNotNull(hMin_max);
			Assert::IsNotNull(hAverage);

			WaitForSingleObject(hMin_max, INFINITE); 
			WaitForSingleObject(hAverage, INFINITE);

			for (int i = 0; i < size; ++i) {
				if (numbers[i] == minValue || numbers[i] == maxValue) {
					numbers[i] = static_cast<int>(averageValue);
				}
			}

			Assert::AreEqual(3, numbers[0]);
			Assert::AreEqual(3, numbers[4]); 

			CloseHandle(hMin_max);
			CloseHandle(hAverage);
			delete[] numbers;
			numbers = nullptr;
		}

		TEST_METHOD(TestMin_MaxThreadWithNegative)
		{
			size = 5;
			numbers = new int[size] { -1, -2, -3, -4, -5 };

			HANDLE hMin_max = CreateThread(NULL, 0, min_max, NULL, 0, NULL);
			Assert::IsNotNull(hMin_max);
			WaitForSingleObject(hMin_max, INFINITE);

			Assert::AreEqual(-5, minValue);
			Assert::AreEqual(-1, maxValue);

			CloseHandle(hMin_max);
			delete[] numbers;
			numbers = nullptr;
		}

		TEST_METHOD(TestAverageThreadWithNegative)
		{
			size = 5;
			numbers = new int[size] { -1, -2, -3, -4, -5 };

			HANDLE hAverage = CreateThread(NULL, 0, average, NULL, 0, NULL);
			Assert::IsNotNull(hAverage);
			WaitForSingleObject(hAverage, INFINITE);

			Assert::AreEqual(-3.0, averageValue, 0.001);

			CloseHandle(hAverage);
			delete[] numbers;
			numbers = nullptr;
		}

		TEST_METHOD(TestReplaceMinMaxWithAverageNegative)
		{
			size = 5;
			numbers = new int[size] { -1, -2, -3, -4, -5 };

			HANDLE hMin_max = CreateThread(NULL, 0, min_max, NULL, 0, NULL);
			HANDLE hAverage = CreateThread(NULL, 0, average, NULL, 0, NULL);

			Assert::IsNotNull(hMin_max);
			Assert::IsNotNull(hAverage);

			WaitForSingleObject(hMin_max, INFINITE);
			WaitForSingleObject(hAverage, INFINITE);

			for (int i = 0; i < size; ++i) {
				if (numbers[i] == minValue || numbers[i] == maxValue) {
					numbers[i] = static_cast<int>(averageValue);
				}
			}

			Assert::AreEqual(-3, numbers[0]);
			Assert::AreEqual(-3, numbers[4]);

			CloseHandle(hMin_max);
			CloseHandle(hAverage);
			delete[] numbers;
			numbers = nullptr;
		}

		TEST_METHOD(TestMin_MaxThreadWithSame)
		{
			size = 4;
			numbers = new int[size] { 2, 2, 2, 2 };

			HANDLE hMin_max = CreateThread(NULL, 0, min_max, NULL, 0, NULL);
			Assert::IsNotNull(hMin_max);
			WaitForSingleObject(hMin_max, INFINITE);

			Assert::AreEqual(2, minValue);
			Assert::AreEqual(2, maxValue);

			CloseHandle(hMin_max);
			delete[] numbers;
			numbers = nullptr;
		}

		TEST_METHOD(TestAverageThreadWithSame)
		{
			size = 4;
			numbers = new int[size] { 2, 2, 2, 2 };

			HANDLE hAverage = CreateThread(NULL, 0, average, NULL, 0, NULL);
			Assert::IsNotNull(hAverage);
			WaitForSingleObject(hAverage, INFINITE);

			Assert::AreEqual(2.0, averageValue, 0.001);

			CloseHandle(hAverage);
			delete[] numbers;
			numbers = nullptr;
		}

		TEST_METHOD(TestReplaceMinMaxWithAverageWithSame)
		{
			size = 4;
			numbers = new int[size] { 2, 2, 2, 2 };

			HANDLE hMin_max = CreateThread(NULL, 0, min_max, NULL, 0, NULL);
			HANDLE hAverage = CreateThread(NULL, 0, average, NULL, 0, NULL);

			Assert::IsNotNull(hMin_max);
			Assert::IsNotNull(hAverage);

			WaitForSingleObject(hMin_max, INFINITE);
			WaitForSingleObject(hAverage, INFINITE);

			for (int i = 0; i < size; ++i) {
				if (numbers[i] == minValue || numbers[i] == maxValue) {
					numbers[i] = static_cast<int>(averageValue);
				}
			}

			Assert::AreEqual(2, numbers[0]);
			Assert::AreEqual(2, numbers[3]);

			CloseHandle(hMin_max);
			CloseHandle(hAverage);
			delete[] numbers;
			numbers = nullptr;
		}

		TEST_METHOD(TestReplaceMinMaxWithAverageWithSingle)
		{
			size = 1;
			numbers = new int[size] { 10 };

			HANDLE hMin_max = CreateThread(NULL, 0, min_max, NULL, 0, NULL);
			HANDLE hAverage = CreateThread(NULL, 0, average, NULL, 0, NULL);

			Assert::IsNotNull(hMin_max);
			Assert::IsNotNull(hAverage);

			WaitForSingleObject(hMin_max, INFINITE);
			WaitForSingleObject(hAverage, INFINITE);

			for (int i = 0; i < size; ++i) {
				if (numbers[i] == minValue || numbers[i] == maxValue) {
					numbers[i] = static_cast<int>(averageValue);
				}
			}

			Assert::AreEqual(10, numbers[0]);
			Assert::AreEqual(10, numbers[0]);

			CloseHandle(hMin_max);
			CloseHandle(hAverage);
			delete[] numbers;
			numbers = nullptr;
		}

		TEST_METHOD(TestReplaceMinMaxWithAverageWithZero)
		{
			size = 4;
			numbers = new int[size] { 2, 0, -2, 0 };

			HANDLE hMin_max = CreateThread(NULL, 0, min_max, NULL, 0, NULL);
			HANDLE hAverage = CreateThread(NULL, 0, average, NULL, 0, NULL);

			Assert::IsNotNull(hMin_max);
			Assert::IsNotNull(hAverage);

			WaitForSingleObject(hMin_max, INFINITE);
			WaitForSingleObject(hAverage, INFINITE);

			for (int i = 0; i < size; ++i) {
				if (numbers[i] == minValue || numbers[i] == maxValue) {
					numbers[i] = static_cast<int>(averageValue);
				}
			}

			Assert::AreEqual(0, numbers[0]);
			Assert::AreEqual(0, numbers[2]);

			CloseHandle(hMin_max);
			CloseHandle(hAverage);
			delete[] numbers;
			numbers = nullptr;
		}
	};
}
