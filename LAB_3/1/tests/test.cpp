#include "pch.h"

DWORD WINAPI marker(LPVOID param);
TEST(MarkerLogicTest, TryMarkWorksCorrectly) {
    int arr[5] = { 0 };
    EXPECT_TRUE(tryMark(arr, 5, 2, 3)); 
    EXPECT_EQ(arr[2], 3);
    EXPECT_FALSE(tryMark(arr, 5, 2, 4)); 
}

TEST(MarkerLogicTest, ResetMarksWorksCorrectly) {
    int arr[5] = { 1, 2, 3, 4, 5 };
    std::vector<int> indices = { 1, 3 };
    resetMarks(indices, arr, 5);
    EXPECT_EQ(arr[1], 0);
    EXPECT_EQ(arr[3], 0);
}

TEST(MarkerThreadTest, ThreadCreatesAndFinishesCorrectly) {
    InitializeCriticalSection(&csT);
    arrT = new int[sizeT]();

    startEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
    stopEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
    exitEvent = CreateEvent(NULL, TRUE, FALSE, NULL);

    threadHandle = CreateThread(NULL, 0, testMarker, reinterpret_cast<LPVOID>(0), 0, NULL); 
    ASSERT_NE(threadHandle, nullptr); 

    SetEvent(startEvent);
    DWORD waitResult = WaitForSingleObject(stopEvent, 2000);
    EXPECT_EQ(waitResult, WAIT_OBJECT_0); 

    SetEvent(exitEvent); 
    WaitForSingleObject(threadHandle, INFINITE); 

    for (int i = 0; i < sizeT; ++i) {
        EXPECT_EQ(arrT[i], 0);
    }

    CloseHandle(startEvent);
    CloseHandle(stopEvent);
    CloseHandle(exitEvent);
    CloseHandle(threadHandle);
    delete[] arrT;
    DeleteCriticalSection(&csT);
}

int main(int argc, char** argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
