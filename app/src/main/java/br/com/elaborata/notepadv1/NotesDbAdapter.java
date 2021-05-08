/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package br.com.elaborata.notepadv1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 *
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class NotesDbAdapter {

    /* Variáveis */
    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table notes (_id integer primary key autoincrement, "
                    + "title text not null, body text not null);";

    private static final String DATABASE_NAME = "data.db";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Fazendo o Update do banco de dados da versão " + oldVersion +
                    " para " + newVersion + ", ação essa que destruirá os dados antigos");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    /**
     * Construtor - recebe o contexto para permitir que o banco de dados seja
     * criado/aberto
     * @param ctx O contexto no qual trabalhar
     */
    public NotesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Abre o banco de dados das notas. Se não pode ser aberto, cria o banco.
     * se não pode ser criado, emita uma exceção com sinal de falha
     *
     * @return this (Referencia a si mesmo, permitindo que seja canalizado para
     *         uma chamada inicial)
     * @throws SQLException se o banco de dados não pode ser aberto ou criado
     */
    public NotesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Cria uma nova nota usando o título e corpo provido. A nota criada com sucesso
     * retorna um novo rowId. Caso contrário, retorna -1 como sinal de falha     *
     * @param title O título da nota
     * @param body O corpo da nota
     * @return Retorna rowId ou -1 se houve falha
     */
    public long createNote(String title, String body) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Exclui uma nota com um dado rowId
     *
     * @param rowId ID da nota a ser excluída
     * @return Retorna true se excluído e false caso não tenha sido excluído
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Retorna um Cursor da lista de todas as notas do banco de dados
     *
     * @return Cursor de todas as notas
     */
    public Cursor fetchAllNotes() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY}, null, null, null, null, null);

    }

    /**
     * Retorna um cursor posicionado na nota que bate com o rowId fornecido
     *
     * @param rowId ID da nota a ser recuperada
     * @return Cursor posicionado na nota selecionada, se encontrada
     * @throws SQLException Se a nota não pôde ser encontrada/recuperada
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                                KEY_TITLE, KEY_BODY}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Faz o Update da nota usando os detalhes providos. A nota a ser atualizada é
     * especificada usando a rowId e é alterada para usar os valores de título e corpo
     * enviados ao método
     *
     * @param rowId Id da nota a ser atualizada
     * @param title de título a ser atualizado na nota
     * @param body Valor a ser usado como corpo da nota
     * @return Retorna true se a nota foi atualizada com sucesso e false em caso contrário.
     */
    public boolean updateNote(long rowId, String title, String body) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}