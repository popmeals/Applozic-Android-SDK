package com.applozic.mobicomkit.api.account.user;

import android.content.Context;

import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * An asynchronous task that returns a list of users for the given search term. Search is done by <i>user-id</i> ({@link User#getUserId()}).
 *
 * <p>Created for async execution of {@link UserService#getUserListBySearch(String)}. Refer to that method for details.
 *
 * <code>
 *     AlUserSearchTask alUserSearchTask = new AlUserSearchTask(context, "yourSearchString", new AlUserSearchHandler() {
 *             @Override
 *             public void onSuccess(List<Contact> contacts, Context context) {
 *                 //Contact firstContact = contacts.get(0);
 *             }
 *
 *             @Override
 *             public void onFailure(Exception exception, Context context) {
 *                 //exception.printStackTrace();
 *             }
 *         });
 *         AlTask.execute(alUserSearchTask);
 *
 *         //for versions prior to v5.95 use:
 *         //alUserSearchTask.execute();
 * </code>
 *
 * Use {@link AlUserSearchHandler#onSuccess(List, Context)} and
 * {@link AlUserSearchHandler#onFailure(Exception, Context)} to get the results.</p>
 */
public class AlUserSearchTask extends AlAsyncTask<Void, List<Contact>> {

    private WeakReference<Context> context;
    private String searchString;
    private Exception exception;
    private UserService userService;
    private AlUserSearchHandler listener;

    /**
     * @param context the context
     * @param searchString the user-id search string for the user/s to search
     * @param listener the callback
     */
    public AlUserSearchTask(Context context, String searchString, AlUserSearchHandler listener) {
        this.context = new WeakReference<>(context);
        this.searchString = searchString;
        this.listener = listener;
        userService = UserService.getInstance(context);
    }

    @Override
    protected List<Contact> doInBackground() {
        if (searchString == null) {
            exception = new ApplozicException("Empty search string");
            return null;
        }

        try {
            return userService.getUserListBySearch(searchString);
        } catch (Exception e) {
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Contact> contactList) {
        super.onPostExecute(contactList);

        if (listener != null) {
            if (contactList != null) {
                listener.onSuccess(contactList, context.get());
            } else {
                listener.onFailure(exception, context.get());
            }
        }
    }

    public interface AlUserSearchHandler {
        void onSuccess(List<Contact> contacts, Context context);

        void onFailure(Exception e, Context context);
    }
}
