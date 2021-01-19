package org.odk.collect.android.widgets.utilities;

import android.app.Activity;
import android.util.Pair;

import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.fakes.FakePermissionsProvider;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.audiorecorder.recorder.Output;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;
import org.odk.collect.audiorecorder.recording.RecordingSession;
import org.odk.collect.testshared.FakeLifecycleOwner;
import org.robolectric.Robolectric;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;

@RunWith(AndroidJUnit4.class)
public class InternalRecordingRequesterTest {

    private final FakePermissionsProvider permissionsProvider = new FakePermissionsProvider();
    private final AudioRecorderViewModel viewModel = mock(AudioRecorderViewModel.class);

    private InternalRecordingRequester requester;

    @Before
    public void setup() {
        Activity activity = Robolectric.buildActivity(Activity.class).get();
        requester = new InternalRecordingRequester(activity, viewModel, permissionsProvider, new FakeLifecycleOwner(), mock(FormEntryViewModel.class));
        permissionsProvider.setPermissionGranted(true);
    }

    @Test
    public void requestRecording_startsWithAAC() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        requester.requestRecording(prompt);

        verify(viewModel).start(prompt.getIndex().toString(), Output.AAC);
    }

    @Test
    public void requestRecording_whenPromptQualityIsVoiceOnly_startsWithAMR() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withBindAttribute("odk", "quality", "voice-only")
                .build();

        requester.requestRecording(prompt);

        verify(viewModel).start(prompt.getIndex().toString(), Output.AMR);
    }

    @Test
    public void requestRecording_whenPromptQualityIsLow_startsWithAACLow() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withBindAttribute("odk", "quality", "low")
                .build();

        requester.requestRecording(prompt);

        verify(viewModel).start(prompt.getIndex().toString(), Output.AAC_LOW);
    }

    @Test
    public void requestRecording_whenPermissionDenied_doesNothing() {
        permissionsProvider.setPermissionGranted(false);

        FormEntryPrompt prompt = promptWithAnswer(null);
        requester.requestRecording(prompt);

        verify(viewModel, never()).start(any(), any());
    }

    @Test
    public void onIsRecordingChangedBlocked_listensToCurrentSession() {
        MutableLiveData<RecordingSession> liveData = new MutableLiveData<>(null);
        when(viewModel.getCurrentSession()).thenReturn(liveData);

        Consumer<Boolean> listener = mock(Consumer.class);
        requester.onIsRecordingBlocked(listener);
        verify(listener).accept(false);

        liveData.setValue(new RecordingSession("blah", null, 0, 0, false));
        verify(listener).accept(true);
    }

    @Test
    public void whenViewModelSessionUpdates_callsInProgressListener() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        MutableLiveData<RecordingSession> sessionLiveData = new MutableLiveData<>(null);
        when(viewModel.getCurrentSession()).thenReturn(sessionLiveData);

        Consumer<Pair<Long, Integer>> listener = mock(Consumer.class);
        requester.onRecordingInProgress(prompt, listener);
        verify(listener).accept(null);

        sessionLiveData.setValue(new RecordingSession(prompt.getIndex().toString(), null, 1200L, 25, false));
        verify(listener).accept(new Pair<>(1200L, 25));
    }

    @Test
    public void whenViewModelSessionUpdates_forDifferentSession_callsInProgressListenerWithNull() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        MutableLiveData<RecordingSession> sessionLiveData = new MutableLiveData<>(null);
        when(viewModel.getCurrentSession()).thenReturn(sessionLiveData);

        Consumer<Pair<Long, Integer>> listener = mock(Consumer.class);
        requester.onRecordingInProgress(prompt, listener);
        verify(listener).accept(null);

        sessionLiveData.setValue(new RecordingSession("something else", null, 1200L, 0, false));
        verify(listener, times(2)).accept(null);
    }
}
