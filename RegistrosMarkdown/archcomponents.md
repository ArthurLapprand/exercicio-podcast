# Android Architeture Components

## ROOM

Para refatorarmos o código para incluir a implementação de Room em nosso aplicativo,
tivemos que incluir as 3 classes relacionadas para sua utilização.

A nossa entidade:

<details>
 <summary>ItemFeedEntity</summary>

```JAVA
@Entity(tableName="episodes")
public class ItemFeedEntity{
    @PrimaryKey()
    private @NonNull String id;

    @ColumnInfo(name=EPISODE_TITLE)
    private String episodeTitle;

    @ColumnInfo(name=EPISODE_DATE)
    private String episodeDate;

    @ColumnInfo(name=EPISODE_DESC)
    private String episodeDesc;

    @ColumnInfo(name=EPISODE_DOWNLOAD_LINK)
    private String episodeDownloadLink;

    @ColumnInfo(name=EPISODE_LINK)
    private String episodeLink;

    @ColumnInfo(name=EPISODE_TIMESTAMP)
    private int episodeTimestamp;

    @ColumnInfo(name=EPISODE_FILE_URI)
    private String episodeFileUri;

    @ColumnInfo(name=EPISODE_DOWNLOAD_STATE)
    private int episodeDownloadState;

    /*
    Getters e Setters, e construtores
}
```
</details>
<br>

O nosso DAO(Data Access Object), responsável por ser a interface com o qual o nosso RoomDatabase interage com a tabela da entidade relacianada ao DAO.

<details>
 <summary>ItemFeedDAO</summary>

```JAVA
@Dao
public interface ItemFeedDAO {

    @Query("SELECT * FROM episodes")
    public List<ItemFeedEntity> getAllEpisodes();

    @Query("SELECT * FROM episodes WHERE downloadLink = :itemDownloadLink")
    public ItemFeedEntity getEpisodeFromDownloadLink(String itemDownloadLink);

    @Query("SELECT * FROM episodes WHERE downloadUri = :itemFileUri")
    public ItemFeedEntity getEpisodeFromFileURI(String itemFileUri);

    @Update()
    public void updateIteemFeed(ItemFeedEntity item);

    @Insert
    public void insertAll(ItemFeedEntity... items);

    @Delete
    public void delete(ItemFeedEntity item);

}
```
</details>
<br>

E nosso RoomDatabase, objeto cuja instância representa o banco de dados SQLite do Android do nosso app. Ele só deve ser instanciado uma única vez, pois uma instância deste objeto é muito pesado na memória.

<details>
 <summary>AppDatabase</summary>

```JAVA
@Database(entities= {ItemFeedEntity.class}, version=1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ItemFeedDAO itemFeedDAO();
}

```
</details>
<br>

A implementação dessas 3 classes facilitou na chamada das funções do SQLite, e ajudou a tornar mais legivel as funções que interagiam com o banco. Nós só precisavamos ter acesso a instância do nosso AppDatabase para podermos realizar as operações definidas nos DAOs. Além de tornar o código muito mais legível, e facilitar a manutenção e expansão do código ao concentrar todas as operações do banco nos DAOs.

<details>
 <summary>Exemplo de código sem Room</summary>
 
```JAVA
ContentValues contentValues = new ContentValues();
contentValues.put(EPISODE_DOWNLOAD_STATE, 1);
getContentResolver().update(
        EPISODE_LIST_URI,
        contentValues,
        EPISODE_DOWNLOAD_LINK + " =? ",
        new String[]{downloadLink}
);

```
</details>

<details>
 <summary>Função equivalente com Room</summary>
 
```JAVA
ItemFeedEntity item = db.itemFeedDAO().getEpisodeFromDownloadLink(downloadLink);
item.setEpisodeDownloadState(1);
db.itemFeedDAO().updateIteemFeed(item);
```
</details>
<br>

O código ficou muito mais intuitivo, por tratar da função SQL no próprio DAO.

## LiveData

Usar o livedata com o estado atual do código é bastante simples, uma vez que apenas substituimos um novo feed por outro. Basta criar a classe LiveItemFeed, que extende a classe ViewModel e armazena a lista de episódios, e usá-la onde for necessário.

<details>
 <summary>LiveItemFeed</summary>

```java
public class LiveItemFeed extends ViewModel {
    private MutableLiveData<List<ItemFeedEntity>> feedLiveData;

    public MutableLiveData<List<ItemFeedEntity>> getFeedLiveData() {
        if (feedLiveData == null) feedLiveData = new MutableLiveData<>();
        return feedLiveData;
    }
}
```
</details>
<br>

No nosso caso, para funcionar só precisamos alterar a MainActivity, instanciando LiveItemFeed e criando um Observer que irá atualizar a lista.

### Mudanças na MainActivity

<details>
 <summary>OnCreate</summary>

```java
liveItemFeed = ViewModelProviders.of(this).get(LiveItemFeed.class);

final Observer feedObserver = new Observer() {
    @Override
    public void onChanged(@Nullable Object o) {
        RoomXmlFeedAdapter adapter = new RoomXmlFeedAdapter(getApplicationContext(), R.layout.itemlista, (List<ItemFeedEntity>) o);
        items.setAdapter(adapter);
    }
};

liveItemFeed.getFeedLiveData().observe(this, feedObserver);
```
</details>

<details>
 <summary>updateRoomTask -> onPostExecute</summary>

```java
// Usando livedata
liveItemFeed.getFeedLiveData().setValue(feed);
items.setTextFilterEnabled(true);
```
</details>
<br>

Naturalmente outras refatorações de código tornariam isto mais interessante, como juntar o LiveData ao Room e dividir as atualizações do feed para inserção de apenas 1 item por vez.