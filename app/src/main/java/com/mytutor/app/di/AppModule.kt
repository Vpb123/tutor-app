package com.mytutor.app.di

import android.content.Context
import com.mytutor.app.data.remote.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mytutor.app.data.remote.repository.*
import com.mytutor.app.domain.usecase.ComputeLessonStatusUseCase;
import com.mytutor.app.domain.usecase.GetTutorDashboardStatsUseCase;
import com.mytutor.app.domain.usecase.GetCourseProgressUseCase;
import com.mytutor.app.domain.usecase.GetCourseCompletionStatusUseCase;
import com.mytutor.app.domain.usecase.SubmitQuizUseCase;
import com.mytutor.app.domain.usecase.CanStudentAccessLessonUseCase;
import com.mytutor.app.utils.imageupload.ImageKitConfig
import com.mytutor.app.utils.imageupload.ImageUploader

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Properties
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = AuthRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        imageUploader: ImageUploader
    ): UserRepository {
        return UserRepository(
            firestore = firestore,
            auth = auth,
            imageUploader = imageUploader
        )
    }

    @Provides
    @Singleton
    fun provideCourseRepository(): CourseRepository = CourseRepository()

    @Provides
    @Singleton
    fun provideEnrolmentRepository(): EnrolmentRepository = EnrolmentRepository()

    @Provides
    @Singleton
    fun provideLessonRepository(): LessonRepository = LessonRepository()

    @Provides
    @Singleton
    fun provideProgressRepository(): ProgressRepository = ProgressRepository()

    @Provides
    @Singleton
    fun provideQuizRepository(): QuizRepository = QuizRepository()

    @Provides
    @Singleton
    fun provideQuizResultRepository(): QuizResultRepository = QuizResultRepository()

 
    @Provides
    @Singleton
    fun provideComputeLessonStatusUseCase(): ComputeLessonStatusUseCase = ComputeLessonStatusUseCase()

    @Provides
    @Singleton
    fun provideCanStudentAccessLessonUseCase(): CanStudentAccessLessonUseCase = CanStudentAccessLessonUseCase()

    @Provides
    @Singleton
    fun provideSubmitQuizUseCase(
        quizResultRepository: QuizResultRepository
    ): SubmitQuizUseCase = SubmitQuizUseCase(quizResultRepository)

    @Provides
    @Singleton
    fun provideGetCourseProgressUseCase(): GetCourseProgressUseCase = GetCourseProgressUseCase()

    @Provides
    @Singleton
    fun provideGetCourseCompletionStatusUseCase(
        quizResultRepository: QuizResultRepository
    ): GetCourseCompletionStatusUseCase = GetCourseCompletionStatusUseCase(quizResultRepository)


    @Provides
    @Singleton
    fun provideGetTutorDashboardStatsUseCase(
        courseRepository: CourseRepository,
        enrolmentRepository: EnrolmentRepository,
        lessonRepository: LessonRepository,
        progressRepository: ProgressRepository,
        quizResultRepository: QuizResultRepository,
        quizRepository: QuizRepository,
        getCourseProgressUseCase: GetCourseProgressUseCase,
        getCourseCompletionStatusUseCase: GetCourseCompletionStatusUseCase
    ): GetTutorDashboardStatsUseCase = GetTutorDashboardStatsUseCase(
        courseRepository,
        enrolmentRepository,
        lessonRepository,
        progressRepository,
        quizResultRepository,
        quizRepository,
        getCourseProgressUseCase,
        getCourseCompletionStatusUseCase
    )

    @Provides
    @Singleton
    fun provideImageKitConfig(@ApplicationContext context: Context): ImageKitConfig {

        return ImageKitConfig(
            publicKey="public_oV+15UMXlxUG/2lbTfiBczJqdOM=",
            endpoint = "https://upload.imagekit.io/api/v1/"
        )
    }

    @Provides
    @Singleton
    fun provideImageUploader(config: ImageKitConfig): ImageUploader {
        return ImageUploader(config)
    }
}
