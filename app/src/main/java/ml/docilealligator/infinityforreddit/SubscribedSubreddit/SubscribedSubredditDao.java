package ml.docilealligator.infinityforreddit.SubscribedSubreddit;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SubscribedSubredditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SubscribedSubredditData subscribedSubredditData);

    @Query("DELETE FROM subscribed_subreddits")
    void deleteAllSubscribedSubreddits();

    @Query("SELECT * from subscribed_subreddits WHERE username = :accountName ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<SubscribedSubredditData>> getAllSubscribedSubreddits(String accountName);

    @Query("SELECT * from subscribed_subreddits WHERE username = :accountName COLLATE NOCASE ORDER BY name COLLATE NOCASE ASC")
    List<SubscribedSubredditData> getAllSubscribedSubredditsList(String accountName);

    @Query("SELECT * from subscribed_subreddits WHERE username = :accountName COLLATE NOCASE AND is_favorite = 1 ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<SubscribedSubredditData>> getAllFavoriteSubscribedSubreddits(String accountName);

    @Query("SELECT * from subscribed_subreddits WHERE name = :subredditName COLLATE NOCASE AND username = :accountName COLLATE NOCASE LIMIT 1")
    SubscribedSubredditData getSubscribedSubreddit(String subredditName, String accountName);

    @Query("DELETE FROM subscribed_subreddits WHERE name = :subredditName COLLATE NOCASE AND username = :accountName COLLATE NOCASE")
    void deleteSubscribedSubreddit(String subredditName, String accountName);
}
