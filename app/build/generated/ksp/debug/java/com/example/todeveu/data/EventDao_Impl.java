package com.example.todeveu.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class EventDao_Impl implements EventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EventEntity> __insertionAdapterOfEventEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public EventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEventEntity = new EntityInsertionAdapter<EventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `events` (`id`,`timestamp`,`dbRelatiu`,`similarityScore`,`vadScore`,`tipusEvent`,`sustainMs`,`cooldownMs`,`dbThreshold`,`speakerThreshold`,`vadThreshold`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EventEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindDouble(3, entity.getDbRelatiu());
        statement.bindDouble(4, entity.getSimilarityScore());
        statement.bindDouble(5, entity.getVadScore());
        statement.bindString(6, entity.getTipusEvent());
        statement.bindLong(7, entity.getSustainMs());
        statement.bindLong(8, entity.getCooldownMs());
        statement.bindDouble(9, entity.getDbThreshold());
        statement.bindDouble(10, entity.getSpeakerThreshold());
        statement.bindDouble(11, entity.getVadThreshold());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM events";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final EventEntity event, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEventEntity.insert(event);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<EventEntity>> allEvents() {
    final String _sql = "SELECT * FROM events ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"events"}, new Callable<List<EventEntity>>() {
      @Override
      @NonNull
      public List<EventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDbRelatiu = CursorUtil.getColumnIndexOrThrow(_cursor, "dbRelatiu");
          final int _cursorIndexOfSimilarityScore = CursorUtil.getColumnIndexOrThrow(_cursor, "similarityScore");
          final int _cursorIndexOfVadScore = CursorUtil.getColumnIndexOrThrow(_cursor, "vadScore");
          final int _cursorIndexOfTipusEvent = CursorUtil.getColumnIndexOrThrow(_cursor, "tipusEvent");
          final int _cursorIndexOfSustainMs = CursorUtil.getColumnIndexOrThrow(_cursor, "sustainMs");
          final int _cursorIndexOfCooldownMs = CursorUtil.getColumnIndexOrThrow(_cursor, "cooldownMs");
          final int _cursorIndexOfDbThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "dbThreshold");
          final int _cursorIndexOfSpeakerThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "speakerThreshold");
          final int _cursorIndexOfVadThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "vadThreshold");
          final List<EventEntity> _result = new ArrayList<EventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final float _tmpDbRelatiu;
            _tmpDbRelatiu = _cursor.getFloat(_cursorIndexOfDbRelatiu);
            final float _tmpSimilarityScore;
            _tmpSimilarityScore = _cursor.getFloat(_cursorIndexOfSimilarityScore);
            final float _tmpVadScore;
            _tmpVadScore = _cursor.getFloat(_cursorIndexOfVadScore);
            final String _tmpTipusEvent;
            _tmpTipusEvent = _cursor.getString(_cursorIndexOfTipusEvent);
            final int _tmpSustainMs;
            _tmpSustainMs = _cursor.getInt(_cursorIndexOfSustainMs);
            final int _tmpCooldownMs;
            _tmpCooldownMs = _cursor.getInt(_cursorIndexOfCooldownMs);
            final float _tmpDbThreshold;
            _tmpDbThreshold = _cursor.getFloat(_cursorIndexOfDbThreshold);
            final float _tmpSpeakerThreshold;
            _tmpSpeakerThreshold = _cursor.getFloat(_cursorIndexOfSpeakerThreshold);
            final float _tmpVadThreshold;
            _tmpVadThreshold = _cursor.getFloat(_cursorIndexOfVadThreshold);
            _item = new EventEntity(_tmpId,_tmpTimestamp,_tmpDbRelatiu,_tmpSimilarityScore,_tmpVadScore,_tmpTipusEvent,_tmpSustainMs,_tmpCooldownMs,_tmpDbThreshold,_tmpSpeakerThreshold,_tmpVadThreshold);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<EventEntity>> eventsToday(final long startOfDay) {
    final String _sql = "SELECT * FROM events WHERE timestamp >= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startOfDay);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"events"}, new Callable<List<EventEntity>>() {
      @Override
      @NonNull
      public List<EventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDbRelatiu = CursorUtil.getColumnIndexOrThrow(_cursor, "dbRelatiu");
          final int _cursorIndexOfSimilarityScore = CursorUtil.getColumnIndexOrThrow(_cursor, "similarityScore");
          final int _cursorIndexOfVadScore = CursorUtil.getColumnIndexOrThrow(_cursor, "vadScore");
          final int _cursorIndexOfTipusEvent = CursorUtil.getColumnIndexOrThrow(_cursor, "tipusEvent");
          final int _cursorIndexOfSustainMs = CursorUtil.getColumnIndexOrThrow(_cursor, "sustainMs");
          final int _cursorIndexOfCooldownMs = CursorUtil.getColumnIndexOrThrow(_cursor, "cooldownMs");
          final int _cursorIndexOfDbThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "dbThreshold");
          final int _cursorIndexOfSpeakerThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "speakerThreshold");
          final int _cursorIndexOfVadThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "vadThreshold");
          final List<EventEntity> _result = new ArrayList<EventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final float _tmpDbRelatiu;
            _tmpDbRelatiu = _cursor.getFloat(_cursorIndexOfDbRelatiu);
            final float _tmpSimilarityScore;
            _tmpSimilarityScore = _cursor.getFloat(_cursorIndexOfSimilarityScore);
            final float _tmpVadScore;
            _tmpVadScore = _cursor.getFloat(_cursorIndexOfVadScore);
            final String _tmpTipusEvent;
            _tmpTipusEvent = _cursor.getString(_cursorIndexOfTipusEvent);
            final int _tmpSustainMs;
            _tmpSustainMs = _cursor.getInt(_cursorIndexOfSustainMs);
            final int _tmpCooldownMs;
            _tmpCooldownMs = _cursor.getInt(_cursorIndexOfCooldownMs);
            final float _tmpDbThreshold;
            _tmpDbThreshold = _cursor.getFloat(_cursorIndexOfDbThreshold);
            final float _tmpSpeakerThreshold;
            _tmpSpeakerThreshold = _cursor.getFloat(_cursorIndexOfSpeakerThreshold);
            final float _tmpVadThreshold;
            _tmpVadThreshold = _cursor.getFloat(_cursorIndexOfVadThreshold);
            _item = new EventEntity(_tmpId,_tmpTimestamp,_tmpDbRelatiu,_tmpSimilarityScore,_tmpVadScore,_tmpTipusEvent,_tmpSustainMs,_tmpCooldownMs,_tmpDbThreshold,_tmpSpeakerThreshold,_tmpVadThreshold);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object countToday(final long startOfDay, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM events WHERE timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startOfDay);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object recentEvents(final int limit,
      final Continuation<? super List<EventEntity>> $completion) {
    final String _sql = "SELECT * FROM events ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EventEntity>>() {
      @Override
      @NonNull
      public List<EventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDbRelatiu = CursorUtil.getColumnIndexOrThrow(_cursor, "dbRelatiu");
          final int _cursorIndexOfSimilarityScore = CursorUtil.getColumnIndexOrThrow(_cursor, "similarityScore");
          final int _cursorIndexOfVadScore = CursorUtil.getColumnIndexOrThrow(_cursor, "vadScore");
          final int _cursorIndexOfTipusEvent = CursorUtil.getColumnIndexOrThrow(_cursor, "tipusEvent");
          final int _cursorIndexOfSustainMs = CursorUtil.getColumnIndexOrThrow(_cursor, "sustainMs");
          final int _cursorIndexOfCooldownMs = CursorUtil.getColumnIndexOrThrow(_cursor, "cooldownMs");
          final int _cursorIndexOfDbThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "dbThreshold");
          final int _cursorIndexOfSpeakerThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "speakerThreshold");
          final int _cursorIndexOfVadThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "vadThreshold");
          final List<EventEntity> _result = new ArrayList<EventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final float _tmpDbRelatiu;
            _tmpDbRelatiu = _cursor.getFloat(_cursorIndexOfDbRelatiu);
            final float _tmpSimilarityScore;
            _tmpSimilarityScore = _cursor.getFloat(_cursorIndexOfSimilarityScore);
            final float _tmpVadScore;
            _tmpVadScore = _cursor.getFloat(_cursorIndexOfVadScore);
            final String _tmpTipusEvent;
            _tmpTipusEvent = _cursor.getString(_cursorIndexOfTipusEvent);
            final int _tmpSustainMs;
            _tmpSustainMs = _cursor.getInt(_cursorIndexOfSustainMs);
            final int _tmpCooldownMs;
            _tmpCooldownMs = _cursor.getInt(_cursorIndexOfCooldownMs);
            final float _tmpDbThreshold;
            _tmpDbThreshold = _cursor.getFloat(_cursorIndexOfDbThreshold);
            final float _tmpSpeakerThreshold;
            _tmpSpeakerThreshold = _cursor.getFloat(_cursorIndexOfSpeakerThreshold);
            final float _tmpVadThreshold;
            _tmpVadThreshold = _cursor.getFloat(_cursorIndexOfVadThreshold);
            _item = new EventEntity(_tmpId,_tmpTimestamp,_tmpDbRelatiu,_tmpSimilarityScore,_tmpVadScore,_tmpTipusEvent,_tmpSustainMs,_tmpCooldownMs,_tmpDbThreshold,_tmpSpeakerThreshold,_tmpVadThreshold);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object allEventsList(final Continuation<? super List<EventEntity>> $completion) {
    final String _sql = "SELECT * FROM events ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EventEntity>>() {
      @Override
      @NonNull
      public List<EventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfDbRelatiu = CursorUtil.getColumnIndexOrThrow(_cursor, "dbRelatiu");
          final int _cursorIndexOfSimilarityScore = CursorUtil.getColumnIndexOrThrow(_cursor, "similarityScore");
          final int _cursorIndexOfVadScore = CursorUtil.getColumnIndexOrThrow(_cursor, "vadScore");
          final int _cursorIndexOfTipusEvent = CursorUtil.getColumnIndexOrThrow(_cursor, "tipusEvent");
          final int _cursorIndexOfSustainMs = CursorUtil.getColumnIndexOrThrow(_cursor, "sustainMs");
          final int _cursorIndexOfCooldownMs = CursorUtil.getColumnIndexOrThrow(_cursor, "cooldownMs");
          final int _cursorIndexOfDbThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "dbThreshold");
          final int _cursorIndexOfSpeakerThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "speakerThreshold");
          final int _cursorIndexOfVadThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "vadThreshold");
          final List<EventEntity> _result = new ArrayList<EventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final float _tmpDbRelatiu;
            _tmpDbRelatiu = _cursor.getFloat(_cursorIndexOfDbRelatiu);
            final float _tmpSimilarityScore;
            _tmpSimilarityScore = _cursor.getFloat(_cursorIndexOfSimilarityScore);
            final float _tmpVadScore;
            _tmpVadScore = _cursor.getFloat(_cursorIndexOfVadScore);
            final String _tmpTipusEvent;
            _tmpTipusEvent = _cursor.getString(_cursorIndexOfTipusEvent);
            final int _tmpSustainMs;
            _tmpSustainMs = _cursor.getInt(_cursorIndexOfSustainMs);
            final int _tmpCooldownMs;
            _tmpCooldownMs = _cursor.getInt(_cursorIndexOfCooldownMs);
            final float _tmpDbThreshold;
            _tmpDbThreshold = _cursor.getFloat(_cursorIndexOfDbThreshold);
            final float _tmpSpeakerThreshold;
            _tmpSpeakerThreshold = _cursor.getFloat(_cursorIndexOfSpeakerThreshold);
            final float _tmpVadThreshold;
            _tmpVadThreshold = _cursor.getFloat(_cursorIndexOfVadThreshold);
            _item = new EventEntity(_tmpId,_tmpTimestamp,_tmpDbRelatiu,_tmpSimilarityScore,_tmpVadScore,_tmpTipusEvent,_tmpSustainMs,_tmpCooldownMs,_tmpDbThreshold,_tmpSpeakerThreshold,_tmpVadThreshold);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
