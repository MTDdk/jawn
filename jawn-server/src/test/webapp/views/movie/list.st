<section>
    <p class="lead">Update the Movie Database by <mark>clicking</mark> on their names</p>
    
    <table class="table table-striped">
        <thead><tr><th>Name</th><th>Year</th></tr></thead>
        
        <tbody>
            $movies:{movie | 
            <tr><td><span class="movie-name" data-type="text" data-pk="$movie.id$" data-url="movie/name">$movie.name$</span></td>
                <td><span class="movie-year" data-type="text" data-pk="$movie.id$" data-url="movie/year">$movie.year$</span></td></tr>
            }$
        </tbody>
    </table>
    
    <p class="lead">Add new movie</p>
    <form class="form-inline" action="/movie/movie" method="post">
        <div class="form-group">
            <label class="sr-only" for="title">Title</label>
            <input type="text" class="form-control" id="title" name="title" placeholder="Title">
        </div>
        <div class="form-group">
            <label class="sr-only" for="year">Year</label>
            <input type="number" min="1000" max="2020" class="form-control" id="year" name="year" placeholder="Year">
        </div>
        <button type="submit" class="btn btn-default">Post</button>
    </form>
</section>