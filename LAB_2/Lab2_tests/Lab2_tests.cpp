#include "pch.h"
#include "CppUnitTest.h"
#include "functions.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace Lab2tests
{
	TEST_CLASS(Lab2tests)
	{
	public:
		
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
	};
}
