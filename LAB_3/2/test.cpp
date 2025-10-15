#include "marker_logic.h"
#include <gtest/gtest.h>  


TEST(MarkerTest, ArrayInitialization) {
    sizeT = 5;
    arrT = new int[sizeT]();

    for (int i = 0; i < sizeT; ++i) {
        EXPECT_EQ(arrT[i], 0); 
    }

    delete[] arrT; 
}

TEST(MarkerTest, TryMarkWorksCorrectly) {
    int arr[5] = { 0 };
    EXPECT_TRUE(tryMark(arr, 5, 2, 3));
    EXPECT_EQ(arr[2], 3);
    EXPECT_FALSE(tryMark(arr, 5, 2, 4));
}

TEST(MarkerTest, ResetMarksWorksCorrectly) {
    int arr[5] = { 1, 2, 3, 4, 5 };
    std::vector<int> indices = { 1, 3 };
    resetMarks(indices, arr, 5);
    EXPECT_EQ(arr[1], 0);
    EXPECT_EQ(arr[3], 0);
}

//TEST(MarkerTest, ThreadsWorkCorrectly) {
//    sizeT = 5;
//    arrT = new int[sizeT]();
//
//    int countOfThreads = 3;
//    std::vector<std::thread> threads;
//
//    threadSignalsT.resize(countOfThreads, false); 
//    threadTerminateT.resize(countOfThreads, false); 
//    threadsFinishedT.resize(countOfThreads, false); 
//
//    threadCVsT.resize(countOfThreads); 
//    threadMutexesT.resize(countOfThreads); 
//    for (int i = 0; i < countOfThreads; i++) { 
//        threadCVsT[i] = std::make_unique<std::condition_variable>(); 
//        threadMutexesT[i] = std::make_unique<std::mutex>(); 
//    }
//
//    for (int i = 0; i < countOfThreads; i++) {
//        threads.emplace_back(testMarker, i);
//    }
//
//    {
//        std::lock_guard<std::mutex> lock(startMutexT);
//        startSignalT = true;
//    }
//    startCVT.notify_all();
//
//    for (auto& thread : threads) {
//        thread.join(); 
//    }
//
//    bool marked = false;
//    for (int i = 0; i < sizeT; ++i) {
//        if (arrT[i] != 0) {
//            marked = true;
//            break;
//        }
//    }
//    EXPECT_TRUE(marked); 
//
//    delete[] arrT; 
//}
//
//TEST(MarkerTest, ThreadTermination) {
//    sizeT = 5;
//    arrT = new int[sizeT]();
//
//    int countOfThreads = 2;
//    std::vector<std::thread> threads;
//
//    threadSignalsT.resize(countOfThreads, false);
//    threadTerminateT.resize(countOfThreads, false);
//    threadsFinishedT.resize(countOfThreads, false);
//
//    threadCVsT.resize(countOfThreads);
//    threadMutexesT.resize(countOfThreads);
//    for (int i = 0; i < countOfThreads; i++) {
//        threadCVsT[i] = std::make_unique<std::condition_variable>();
//        threadMutexesT[i] = std::make_unique<std::mutex>();
//    }
//
//    for (int i = 0; i < countOfThreads; i++) {
//        threads.emplace_back(testMarker, i);
//    }
//
//    {
//        std::lock_guard<std::mutex> lock(startMutexT);
//        startSignalT = true;
//    }
//    startCVT.notify_all();
//
//    std::this_thread::sleep_for(std::chrono::milliseconds(100));
//
//    {
//        std::lock_guard<std::mutex> lock(arrMutexT);
//        arrT[0] = 1; 
//    }
//
//    for (auto& thread : threads) {
//        thread.join(); 
//    }
//
//    EXPECT_NE(arrT[0], 0); 
//
//    delete[] arrT;
//}

int main(int argc, char** argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}